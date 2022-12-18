Una vez hemos terminado de realizar las APIs, hemos generado una imagen 
con los .jar individuales de cada servicio. Una vez hemos realizado las 
imagenes hemos subido las imagenes al DockerHub para poder desplegarlas 
desde kubernetes.

Ahora para desplegar los servicios solo tenemos que generar un entorno 
de kubernetes (1 maestro y 2 workers p.e.) y desplegarlos con 
    kubectl apply -f k8s/

Cada servicio tiene su propio entrypoint.

Para cargar el fichero de discos.json, debemos cambiar la url del 
loaddata.py al del clusterip en el que se haya desplegado y ejecutarlo
con 
    python3 loaddata.py
habiendo instalado los requirimientos previos.