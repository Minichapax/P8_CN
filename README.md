# Práctica 8 Computación na Nube
Repositorio para la práctica 8 de la asignatura de Computación en la Nube.


## Autores:
- Alberto Pérez Hernández
- Iván Doce Franco
- Paulo Antón Gómez Prieto


## Conexión a MongoDB
- Empleando el cliente MongoDB Compass podemos simplemente emplear la URI
`mongodb+srv://USUARIO:CONTRASENA@compnube.t98giok.mongodb.net/?` reemplazando el valor de USUARIO y CONTRASEÑA por los que
cada uno de los usuarios posee.

- Empleando la aplicación Spring, debemos modificar la línea `spring.data.mongodb.uri` del fichero [application.properties](./src/main/resources/application.properties) y reemplazar los valores 
USUARIO y CONTRASEÑA de la misma forma que si estuviéramos en el cliente MongoDB.

**IMPORTANTE: No realizar commit de las modificaciones realizadas al fichero 
[application.properties](./src/main/resources/application.properties) para evitar filtrar las credenciales.**


## Carga de datos en MongoDB
Todos los ficheros de datos que forman el dataset pueden encontrarse en la carpeta [data](./data). Para cargar un fichero de datos
en MongoDB, simplemente nos conectaremos mediante Compass y buscamos nuestra base de datos. Dentro de la base de datos pinchamos sobre
la colección que queremos importar y empleamos el botón `Add Data > Import File`, seleccionamos el fichero JSON correspondiente y ya 
tenemos nuestros datos cargados.