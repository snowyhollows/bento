package net.snowyhollows.bento;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
import net.snowyhollows.bento2.Bento;
import net.snowyhollows.bento2.annotation.BentoWrapper;
import net.snowyhollows.bento2.annotation.ByFactory;
import net.snowyhollows.bento2.annotation.ByName;
import net.snowyhollows.bento2.annotation.WithFactory;

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

  private ClassName factoryNameFor(ClassName type, String suffix) {
    return ClassName.get(type.packageName(), type.simpleName() + suffix);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    Set<? extends Element> wrapperInterfaces =
        roundEnv.getElementsAnnotatedWith(BentoWrapper.class);

    for (Element element : wrapperInterfaces) {
      messager.printMessage(Diagnostic.Kind.NOTE, "generating " + element);
      TypeElement wrapperInterface = (TypeElement) element;

      ClassName factoryName = factoryNameFor(ClassName.get(wrapperInterface), "Impl");

      List<MethodSpec.Builder> methodBuilders = new ArrayList<>();

      for (Element enclosedElement : wrapperInterface.getEnclosedElements()) {
        if (enclosedElement instanceof ExecutableElement) {
          ExecutableElement method = (ExecutableElement) enclosedElement;
          messager.printMessage(Diagnostic.Kind.NOTE, "found method " + element);

          TypeName returnType = ClassName.get(method.getReturnType());
          Builder builder =
              MethodSpec.methodBuilder(method.getSimpleName().toString())
                  .addModifiers(Modifier.PUBLIC)
                  .addAnnotation(Override.class)
                  .addStatement("$T scope = bento.create()", Bento.class)
                  .returns(returnType);

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

      String packageName = ClassName.get(wrapperInterface).packageName();

      TypeSpec.Builder factory =
          TypeSpec.classBuilder(factoryName)
              .addModifiers(Modifier.PUBLIC)
              .addSuperinterface(ClassName.get(wrapperInterface))
              .addField(ClassName.get(Bento.class), "bento", Modifier.FINAL, Modifier.PRIVATE)
              .addMethod(
                  MethodSpec.constructorBuilder()
                      .addModifiers(Modifier.PUBLIC)
                      .addAnnotation(WithFactory.class)
                      .addParameter(ClassName.get(Bento.class), "bento")
                      .addCode("this.bento = bento;\n")
                      .build());

      for (Builder methodBuilder : methodBuilders) {
        factory.addMethod(methodBuilder.build());
      }

      try {
        JavaFile.builder(packageName, factory.build()).build().writeTo(filer);
      } catch (IOException e) {
        throw new RuntimeException(e);
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
    return Collections.singleton(BentoWrapper.class.getName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
