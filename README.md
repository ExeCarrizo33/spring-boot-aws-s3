# Integración de AWS S3 en Spring Boot

Este proyecto demuestra cómo integrar Amazon S3 en una aplicación Spring Boot para operaciones de carga y descarga de archivos.

## Tecnologías y Herramientas Utilizadas

- **Lenguaje de Programación**: Java
- **Framework**: Spring Boot
- **Servicios en la Nube**: Amazon S3
- **Gestión de Dependencias**: Maven
- **Control de Versiones**: Git

## Dependencias Principales

Las dependencias clave utilizadas en este proyecto incluyen:

- `spring-boot-starter-web`: Para construir aplicaciones web RESTful.
- `aws-java-sdk-s3`: Para interactuar con el servicio Amazon S3.

Estas dependencias se gestionan a través de Maven y se especifican en el archivo `pom.xml`.

## Configuración

Para configurar la aplicación con tus credenciales de AWS y el bucket de S3, sigue estos pasos:

1. **Credenciales de AWS**: Asegúrate de tener configuradas tus credenciales de AWS en tu entorno. Puedes hacerlo mediante el archivo `~/.aws/credentials` o configurando las variables de entorno `AWS_ACCESS_KEY_ID` y `AWS_SECRET_ACCESS_KEY`.

2. **Archivo de Propiedades**: En el archivo `src/main/resources/application.properties`, configura los siguientes parámetros:

   ```properties
   cloud.aws.region.static=us-east-1
   cloud.aws.s3.bucket=bucket-name
