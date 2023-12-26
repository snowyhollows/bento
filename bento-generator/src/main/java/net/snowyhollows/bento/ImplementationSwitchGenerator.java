package net.snowyhollows.bento;

import com.squareup.javapoet.*;
import net.snowyhollows.bento.annotation.GwtIncompatible;
import net.snowyhollows.bento.annotation.ImplementationSwitch;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class ImplementationSwitchGenerator extends AbstractProcessor {

    private Filer filer;

    private final static ClassName BENTO_FACTORY = ClassName.get(BentoFactory.class);
    private static final ClassName BENTO_EXCEPTION = ClassName.get(BentoException.class);
    private static final ClassName REFLECTIVE_EXCEPTION = ClassName.get(ReflectiveOperationException.class);

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
    }

    private ClassName factoryNameFor(ClassName type, String suffix) {
        return ClassName.get(type.packageName(), type.simpleName() + suffix);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> pickerInterfaces =
                roundEnv.getElementsAnnotatedWith(ImplementationSwitch.class);

        for (Element element : pickerInterfaces) {
            TypeElement pickerInterface = (TypeElement) element;
            ClassName pickerClassName = ClassName.get(pickerInterface);

            ClassName factoryName = factoryNameFor(ClassName.get(pickerInterface), "Factory");
            String packageName = factoryName.packageName();
            ParameterizedTypeName bentoFactoryParametrized = ParameterizedTypeName.get(BENTO_FACTORY, pickerClassName);

            ImplementationSwitch implementationSwitch = element.getAnnotation(ImplementationSwitch.class);
            String configKey = implementationSwitch.configKey();

            CodeBlock.Builder code = CodeBlock.builder();

            if (configKey.equals(ImplementationSwitch.NO_CONFIG_KEY_DEFINED) && implementationSwitch.cases().length == 0) {
                code.addStatement("throw new $T($S)", BENTO_EXCEPTION, "Implementation of " + pickerClassName + " must be registered manually, e.g. by calling bento.register(" + factoryName.simpleName() + ".IT, someImplementation)");
            } else {
                Optional<ImplementationSwitch.When> firstDefault = Arrays.stream(implementationSwitch.cases()).filter(w -> w.useByDefault()).findFirst();

                if (!firstDefault.isPresent()) {
                    code.addStatement("String configValue = bento.getString($S)", configKey);
                } else {
                    code.addStatement("String configValue = bento.get($S, $S)", configKey, firstDefault.get().name());
                }

                code.beginControlFlow("switch(configValue)");

                for (ImplementationSwitch.When when : implementationSwitch.cases()) {
                    code.addStatement("case $S: return bento.get($T.IT)", when.name(), factoryNameFor((ClassName) ClassName.get(extractClassName(when)), "Factory"));
                }
                String classNameRegexp = "([a-z][a-z0-9]*[.])+[A-Z][A-Za-z0-9]*";
                code.add("default: ")
                        .indent()
                        .beginControlFlow("if (configValue.matches($S))", classNameRegexp)
                        .addStatement("BentoFactory<?> it = instantiateDynamic(configValue)")
                        .addStatement("return ($T)bento.get(it)", pickerClassName)
                        .endControlFlow()
                        .unindent();

                code.endControlFlow();
                code.addStatement("String message = $S + configValue + $S", "No case found for [", "]");
                code.addStatement("throw new $T(message)", BENTO_EXCEPTION);
            }

            MethodSpec.Builder createInContext = MethodSpec.methodBuilder("createInContext")
                    .addParameter(ParameterSpec.builder(Bento.class, "bento").build())
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(code.build())
                    .returns(pickerClassName);

            MethodSpec.Builder instantiateDynamic = MethodSpec.methodBuilder("instantiateDynamic")
                    .addAnnotation(GwtIncompatible.class)
                    .addParameter(ParameterSpec.builder(String.class, "className").build())
                    .addModifiers(Modifier.PRIVATE)
                    .addCode(CodeBlock.builder()
                            .beginControlFlow("try")
                            .addStatement("return (BentoFactory<?>) Class.forName(className + $S).getField($S).get(null)", "Factory", "IT")
                            .nextControlFlow("catch ($T e)", REFLECTIVE_EXCEPTION)
                            .addStatement("throw new $T($S + className + $S)", BENTO_EXCEPTION, "Cold not instantiate class ", " using a default BentoFactory")
                            .endControlFlow()
                            .build())
                    .returns(ParameterizedTypeName.get(ClassName.get(BentoFactory.class), WildcardTypeName.subtypeOf(Object.class)));

            MethodSpec.Builder instantiateDynamicDummy = MethodSpec.methodBuilder("instantiateDynamic")
                    .addParameter(ParameterSpec.builder(Object.class, "unused").build())
                    .addModifiers(Modifier.PRIVATE)
                    .addStatement("throw new UnsupportedOperationException($S)", "Dynamic configuration is not supported in environments without reflection")
                    .returns(ParameterizedTypeName.get(ClassName.get(BentoFactory.class), WildcardTypeName.subtypeOf(Object.class)));

            TypeSpec.Builder factory = TypeSpec.enumBuilder(factoryName)
                    .addModifiers(Modifier.PUBLIC)
                    .addEnumConstant("IT")
                    .addSuperinterface(bentoFactoryParametrized)
                    .addMethod(createInContext.build())
                    .addMethod(instantiateDynamic.build())
                    .addMethod(instantiateDynamicDummy.build());
            try {
                JavaFile.builder(packageName, factory.build()).build().writeTo(filer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private TypeMirror extractClassName(ImplementationSwitch.When when) {
        try {
            when.implementation();
        } catch (MirroredTypeException mte) {
            TypeMirror typeMirror = mte.getTypeMirror();
            return typeMirror;
        }
        return null; // should never happen
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ImplementationSwitch.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
