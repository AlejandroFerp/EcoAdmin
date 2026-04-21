# Getting Started

## Arrancar el servidor en este equipo (Windows — Java 8 en PATH)

Este equipo tiene Java 8 como JDK de sistema. Spring Boot 4 requiere Java 21.
**Java 21** disponible en: `C:\Users\afp5\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64`

### Método 1 — Batch temporal (recomendado)

Crear `%TEMP%\run-spring.bat`:
```bat
@echo off
SET JAVA_HOME=C:\Users\afp5\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64
SET PATH=%JAVA_HOME%\bin;%PATH%
cd /d C:\Users\afp5\Git\servidor_api\servidor_api\ServidorApiRest
"C:\Users\afp5\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798\bin\mvn.cmd" spring-boot:run -q
```

Ejecutar desde PowerShell:
```powershell
cmd /c "$env:TEMP\run-spring.bat"
```

### Método 2 — PowerShell directo (si mvn.cmd está en PATH)

```powershell
$env:JAVA_HOME = "C:\Users\afp5\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Set-Location "C:\Users\afp5\Git\servidor_api\servidor_api\ServidorApiRest"
.\mvnw.cmd spring-boot:run
```

> **Nota**: `mvnw.cmd` ejecuta cmd.exe internamente y hereda el JAVA_HOME del registro de Windows (Java 8).
> El Método 1 lo sobreescribe a nivel de proceso antes de invocar Maven.



### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.9/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.9/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.9/reference/web/servlet.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.5.9/reference/using/devtools.html)
* [Thymeleaf](https://docs.spring.io/spring-boot/3.5.9/reference/web/servlet.html#web.servlet.spring-mvc.template-engines)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.5.9/reference/data/sql.html#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

