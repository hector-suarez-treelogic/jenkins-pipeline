# Comienzo del tutorial

precondiciones tener instalado virtualbox, usuario de github y dockerhub.

arrancamos la maquina usuario osboxes / osboxes.org

Descargar el repositorio

Abrimos una terminal y creamos una carpeta cursodevops

mkdir cursodevops
cd cursodevops

git clone https://github.com/hector-suarez-treelogic/jenkins-pipeline.git


cd jenkins-pipeline

vamos a ejecutar la creación del entorno de trabajo con jenkins, sonar etc ...


``docker-compose.yml``
```yml
version: '3.2'
services:
  sonarqube:
    build:
      context: sonarqube/
    ports:
      - 9000:9000
      - 9092:9092
    container_name: sonarqube
  jenkins:
    build:
      context: jenkins/
    privileged: true
    user: root
    ports:
      - 8080:8080
      - 50000:50000
    container_name: jenkins
    volumes:
      - /tmp/jenkins:/var/jenkins_home #Remember that, the tmp directory is designed to be wiped on system reboot.
      - /var/run/docker.sock:/var/run/docker.sock
    depends_on:
      - sonarqube
```

Para ejecutar este fichero utilizamos el docker compose como sigue

sudo docker-compose -f docker-compose.yml up --build

Cuando acaba todo el proceso verificamos que estan corriendo los nodos

docker ps

## Jenkins

Abrimos un explorador y accedemos a Jenkins

http://localhost:8080/

![](images/004.png)

Para confirmar el acceso tenemos que pegar un texto que aparece en la traza de la consola  ( tambien se almacena dentro del contenedor en :  ``/var/jenkins_home/secrets/initialAdminPassword`` )


```
jenkins      | *************************************************************
jenkins      |
jenkins      | Jenkins initial setup is required. An admin user has been created and a password generated.
jenkins      | Please use the following password to proceed to installation:
jenkins      |
jenkins      | 45638c79cecd4f43962da2933980197e
jenkins      |
jenkins      | This may also be found at: /var/jenkins_home/secrets/initialAdminPassword
jenkins      |
jenkins      | *************************************************************
```

Por si acaso para acceder al fichero dentro del contenedor desde una nueva consola serviria con escribir.

```
docker exec -it jenkins sh
/ $ cat /var/jenkins_home/secrets/initialAdminPassword
```

Despues de instalar los plugins por defecto no creamos usuario y continuamos como admin (en realidad podremos entrar como usuario admin y con password el texto copiado anteriormente)

Accedemos a la parte de gestion y actualizamos el sistema. Nos pide logarnos de nuevo como admin y con pass la que utilizamos anteriormente

Accedemos a la parte de herramientas y tenemos que instalar Docker y Maven

Configuramos los nombres de las herramientas como myMaven y myDocker

Despues habilitamos las credenciales de nuestro repositorio cursodevops en docker hub









* Code checkout
* Run tests
* Compile the code
* Run Sonarqube analysis on the code
* Create Docker image
* Push the image to Docker Hub
* Pull and run the image



## GitHub configuration
We’ll define a service on Github to call the ``Jenkins Github webhook`` because we want to trigger the pipeline. To do this go to _Settings -> Integrations & services._ The ``Jenkins Github plugin`` should be shown on the list of available services as below.

![](images/001.png)

After this, we should add a new service by typing the URL of the dockerized Jenkins container along with the ``/github-webhook/`` path.

![](images/002.png)

The next step is that create an ``SSH key`` for a Jenkins user and define it as ``Deploy keys`` on our GitHub repository.

![](images/003.png)

If everything goes well, the following connection request should return with a success.
```
ssh git@github.com
PTY allocation request failed on channel 0
Hi <your github username>/<repository name>! You've successfully authenticated, but GitHub does not provide shell access.
Connection to github.com closed.
```





We use the value we entered in the ``ID`` field to Docker Login in the script file. Now, we define pipeline under _Jenkins Home Page -> New Item_ menu.

![](images/010.png)

In this step, we select ``GitHub hook trigger for GITScm pooling`` options for automatic run of the pipeline by ``Github hook`` call.

![](images/011.png)

Also in the Pipeline section, we select the ``Pipeline script from SCM`` as Definition, define the GitHub repository and the branch name, and specify the script location (_[Jenkins file](https://github.com/hakdogan/jenkins-pipeline/blob/master/Jenkinsfile)_).

![](images/012.png)

After that, when a push is done to the remote repository or when you manually trigger the pipeline by ``Build Now`` option, the steps described in Jenkins file will be executed.

![](images/013.png)

## Review important points of the Jenkins file

```
stage('Initialize'){
    def dockerHome = tool 'myDocker'
    def mavenHome  = tool 'myMaven'
    env.PATH = "${dockerHome}/bin:${mavenHome}/bin:${env.PATH}"
}
```

The ``Maven`` and ``Docker client`` tools we have defined in Jenkins under _Global Tool Configuration_ menu are added to the ``PATH environment variable`` for using these tools with ``sh command``.

```
stage('Push to Docker Registry'){
    withCredentials([usernamePassword(credentialsId: 'dockerHubAccount', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        pushToImage(CONTAINER_NAME, CONTAINER_TAG, USERNAME, PASSWORD)
    }
}
```

``withCredentials`` provided by ``Jenkins Credentials Binding Plugin`` and bind credentials to variables. We passed **dockerHubAccount** value with ``credentialsId`` parameter. Remember that, dockerHubAccount value is Docker Hub credentials ID we have defined it under _Jenkins Home Page -> Credentials -> Global credentials (unrestricted) -> Add Credentials_ menu. In this way, we access to the username and password information of the account for login.

## Sonarqube configuration

For ``Sonarqube`` we have made the following definitions in the ``pom.xml`` file of the project.

```xml
<sonar.host.url>http://sonarqube:9000</sonar.host.url>
...
<dependencies>
...
    <dependency>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>sonar-maven-plugin</artifactId>
        <version>2.7.1</version>
        <type>maven-plugin</type>
    </dependency>
...
</dependencies>
```

In the docker compose file, we gave the name of the Sonarqube service which is ``sonarqube``, this is why in the ``pom.xml`` file, the sonar URL was defined as http://sonarqube:9000.