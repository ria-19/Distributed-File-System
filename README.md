# gfs

### To override system properties defined in application.propperties file :
```mvn spring-boot:run -Dspring-boot.run.arguments=--<property_name>=<property_value> ```  
Eg.  
```mvn spring-boot:run -Dspring-boot.run.arguments=--chunkserver.port=8020```

