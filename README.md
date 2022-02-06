# gfs

### To override system properties defined in application.propperties file :
```mvn spring-boot:run -Dspring-boot.run.arguments=--<property_name>=<property_value> ```  
Eg.  
```mvn spring-boot:run -Dspring-boot.run.arguments=--chunkserver.port=8020```

###To run multiple chunkservers, deploy and run :
```java -jar <jar_file_path> <main_class_path> --spring.config.location=src/main/resources/chunkserver<1/2/3>.properties```