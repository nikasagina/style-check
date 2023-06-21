package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE)
public class StyleCheckMojo extends AbstractMojo {

    private static final String SNAKE_CASE_REGEX = "[a-z]+(_[a-z]+)*";
    private static final String CAMEL_CASE_REGEX = "[a-z]+([A-Z][a-zA-Z0-9]*)*";
    private static final String PASCAL_CASE_REGEX = "[a-z]+(_[a-z]+)*";

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        Collection<File> files = FileUtils.listFiles(project.getBasedir(), new String[]{"java"}, true);
        for (File javaFile : files) {
            checkFile(javaFile);
        }
    }

    public void checkFile(File javaFile) throws MojoExecutionException {
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            // Parameters
            List<com.github.javaparser.ast.body.Parameter> all = cu.findAll(com.github.javaparser.ast.body.Parameter.class);
            for (com.github.javaparser.ast.body.Parameter variableDeclarator : all) {
                checkVariableName(variableDeclarator.getName().toString());
            }

            // Variables
            List<VariableDeclarator> all1 = cu.findAll(VariableDeclarator.class);
            for (VariableDeclarator variableDeclarator : all1) {
                checkVariableName(variableDeclarator.getName().toString());
            }

            // Methods
            List<MethodDeclaration> all2 = cu.findAll(MethodDeclaration.class);
            for (MethodDeclaration methodDeclaration : all2) {
                checkMethodName(methodDeclaration.getName().toString());
            }

            // Classes
            List<ClassOrInterfaceDeclaration> all3 = cu.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : all3) {
                checkClassName(classOrInterfaceDeclaration.getNameAsString());
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkMethodName(String name) throws MojoExecutionException {
        if (!name.matches(SNAKE_CASE_REGEX)) {
            throw new MojoExecutionException("Code style violated: method name must use snake_case: " + name);
        }
    }

    private void checkVariableName(String name) throws MojoExecutionException {
        if (!name.matches(CAMEL_CASE_REGEX)) {
            throw new MojoExecutionException("Code style violated: variable name must use camelCase: " + name);
        }
    }

    private void checkClassName(String name) throws MojoExecutionException {
        if (!name.matches(PASCAL_CASE_REGEX)) {
            throw new MojoExecutionException("Code style violated: class name must use PascalCase: " + name);
        }
    }
}
