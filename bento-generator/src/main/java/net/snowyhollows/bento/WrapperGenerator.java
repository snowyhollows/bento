package net.snowyhollows.bento;

import com.squareup.javapoet.*;
import com.squareup.javapoet.MethodSpec.Builder;

import java.io.IOException;
import java.util.*;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import net.snowyhollows.bento.annotation.*;

public class WrapperGenerator extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Types types;
    private static final ClassName BENTO = ClassName.get(Bento.class);
    private static final ClassName STRING = ClassName.get(String.class);

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        types = processingEnvironment.getTypeUtils();
    }

    private ClassName suffixedName(ClassName type, String suffix) {
        return ClassName.get(type.packageName(), type.simpleName() + suffix);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<Element> elements = new HashSet<>();


        elements.addAll(roundEnv.getElementsAnnotatedWith(BentoWrapper.class));
        elements.addAll(roundEnv.getElementsAnnotatedWith(LazyProvider.class));

        for (Element element : elements) {
            TypeElement factory = (TypeElement) element;

            boolean lazyProvider = factory.getAnnotation(LazyProvider.class) != null;

            ClassName wrapperClassName = ClassName.get(factory);
            ClassName wrapperImplementationName = suffixedName(wrapperClassName, "Impl");

            List<MethodSpec.Builder> methodBuilders = new ArrayList<>();

            for (Element enclosedElement : factory.getEnclosedElements()) {
                if (enclosedElement instanceof ExecutableElement) {
                    ExecutableElement method = (ExecutableElement) enclosedElement;

                    TypeName returnType = ClassName.get(method.getReturnType());
                    Builder builder =
                            MethodSpec.methodBuilder(method.getSimpleName().toString())
                                    .addModifiers(Modifier.PUBLIC)
                                    .addAnnotation(Override.class)
                                    .returns(returnType);

                    if (lazyProvider) {
                        builder.addStatement("$T scope = bento", Bento.class);
                    } else {
                        builder.addStatement("$T scope = bento.create()", Bento.class);
                    }

                    List<? extends VariableElement> parameters = method.getParameters();

                    for (VariableElement parameter : parameters) {
                        builder.addParameter(
                                ClassName.get(parameter.asType()), parameter.getSimpleName().toString());
                        builder.addStatement(
                                "scope.register($S, $L)",
                                parameter.getSimpleName().toString(),
                                parameter.getSimpleName().toString());
                    }

                    ByFactory byFactory = method.getAnnotation(ByFactory.class);
                    ByName byName = method.getAnnotation(ByName.class);

                    if (byFactory != null) {
                        builder.addStatement("return scope.get($T.IT)", getT(byFactory));
                    }
                    if (returnType.equals(BENTO)) {
                        builder.addStatement("return scope");
                    } else {
                        TypeName typeName = returnType;
                        boolean isEnum =
                                !typeName.isPrimitive()
                                        && types.asElement(method.getReturnType()).getKind() == ElementKind.ENUM;
                        boolean isByName =
                                byName != null || typeName.isPrimitive() || typeName.equals(STRING) || isEnum;
                        if (!isByName) {
                            builder.addStatement(
                                    "return scope.get($T.IT)",
                                    FactoryGenerator.factoryNameFor((ClassName) returnType, "Factory"));
                        } else {
                            String nameToGet =
                                    (byName == null || byName.value().equals("##"))
                                            ? method.getSimpleName().toString()
                                            : byName.value();

                            String call = null;
                            if (typeName.equals(TypeName.FLOAT)) {
                                call = "return bento.getFloat($S)";
                            } else if (typeName.equals(TypeName.INT)) {
                                call = "return bento.getInt($S)";
                            } else if (typeName.equals(TypeName.BOOLEAN)) {
                                call = "return bento.getBoolean($S)";
                            } else if (typeName.equals(STRING)) {
                                call = "return bento.getString($S)";
                            } else if (isEnum) {
                                call = "return bento.getEnum($T.class, $S)";
                            } else {
                                call = "return bento.get($S)";
                            }
                            if (!isEnum) {
                                builder.addStatement(call, nameToGet);
                            } else {
                                builder.addStatement(call, returnType, nameToGet);
                            }
                        }
                    }
                    methodBuilders.add(builder);
                }
            }

            String packageName = wrapperClassName.packageName();

            TypeSpec.Builder wrapperImplementation =
                    TypeSpec.classBuilder(wrapperImplementationName)
                            .addModifiers(Modifier.PUBLIC)
                            .addSuperinterface(wrapperClassName)
                            .addField(ClassName.get(Bento.class), "bento", Modifier.FINAL, Modifier.PRIVATE)
                            .addMethod(
                                    MethodSpec.constructorBuilder()
                                            .addModifiers(Modifier.PUBLIC)
                                            .addAnnotation(WithFactory.class)
                                            .addParameter(ClassName.get(Bento.class), "bento")
                                            .addCode("this.bento = bento;\n")
                                            .build());

            for (Builder methodBuilder : methodBuilders) {
                wrapperImplementation.addMethod(methodBuilder.build());
            }

            ParameterizedTypeName bentoFactoryParametrized = ParameterizedTypeName.get(FactoryGenerator.BENTO_FACTORY, wrapperClassName);

            MethodSpec.Builder createInContext = MethodSpec.methodBuilder("createInContext")
                    .addParameter(ParameterSpec.builder(Bento.class, "bento").build())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(wrapperClassName)
                    .addStatement("return $T.IT.createInContext(bento)", suffixedName(wrapperImplementationName, "Factory"));

            TypeSpec.Builder factoryImplementation = TypeSpec.enumBuilder(suffixedName(wrapperClassName, "Factory"))
                    .addModifiers(Modifier.PUBLIC)
                    .addEnumConstant("IT")
                    .addSuperinterface(bentoFactoryParametrized)
                    .addMethod(createInContext.build());

            try {
                JavaFile.builder(packageName, factoryImplementation.build()).build().writeTo(filer);
                JavaFile.builder(packageName, wrapperImplementation.build()).build().writeTo(filer);
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }
        return true;
    }

    private static TypeMirror getT(ByFactory byFactory) {
        try {
            byFactory.value();
        } catch (MirroredTypeException mte) {

            return mte.getTypeMirror();
        }
        return null;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Arrays.asList(BentoWrapper.class.getName(), LazyProvider.class.getName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
