# [Jakarta JSON Binding](https://projects.eclipse.org/projects/ee4j.jsonb) + [Eclipse Yasson](https://projects.eclipse.org/projects/ee4j.jsonb) + [Java Records](https://openjdk.java.net/jeps/395)

| Component                   | Version                                                                                                                                                                            |
|-----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java Development Kit        | [JDK 17](https://openjdk.java.net/projects/jdk/17/) LTS ([Long-term Support](https://openjdk.java.net/jeps/322))                                                                   |
| JSON Binding Specification  | [Jakarta JSON Binding 2.0.0](https://jakarta.ee/specifications/jsonb/2.0/jakarta-jsonb-spec-2.0.html) (part of [Jakarta EE 9](https://projects.eclipse.org/releases/jakarta-ee-9)) |
| JSON Binding Implementation | [Eclipse Yasson 2.0.2](https://mvnrepository.com/artifact/org.eclipse/yasson/2.0.2)                                                                                                |
| Build Automation Tool       | [Apache Maven 3.6.3+](https://maven.apache.org/)                                                                                                                                          |

## Motivation
Java Records are great match for representing [DTO](https://en.wikipedia.org/wiki/Data_transfer_object) in [REST](https://en.wikipedia.org/wiki/Representational_state_transfer)
communication. This project explores what is the minimal setup for successful serialization and deserialization of Java Records to and from
[JSON](https://en.wkipedia.org/wiki/JSON) representation.

## Inspiration
Adam Bien's blog post about this topic: https://adambien.blog/roller/abien/entry/serializing_and_deserializing_java_records

## Implementation

### Java Record
Since Java Records are immutable the JSON-B implementation cannot use default constructor to create empty instance and then set the properties using fields or methods. All
values have to be provided at once. We can use static initializers as it was shown in Adam's blog post or constructor. It is particularly useful to mark record's
[compact constructor](https://javaalmanac.io/features/records/#constructors-canonical-custom-and-compact) with @JsonbCreator annotation. This way we don't need to repeat record
components again as parameters.

```java
public record Workshop(String title, LocalDateTime date, String description) {

    @JsonbCreator
    public Workshop {}

}
```

There are some limitation to this approach:
1. Absent JSON fields that could represent `null` or some default values in Java objects cannot be used with current JSON-B specification (see section
   [4.5 Custom instantiation](https://jakarta.ee/specifications/jsonb/2.0/jakarta-jsonb-spec-2.0.html#custom-instantiation)).  
   > In case a field required for a parameter mapping doesn’t exist in JSON document, JsonbException MUST be thrown.  
   
   This is unfortunate in case when we don't have control over consumed JSON. This problem is well-known and it is discussed for example here:
   https://github.com/eclipse-ee4j/jsonb-api/issues/121  
   
   Solution should be part of Pull Request: https://github.com/eclipse-ee4j/jsonb-api/pull/285 and it seems will be part of Jakarta JSON Binding 2.1.0. It seems that change
   of annotation over compact constructor will help:
   ```java
   @JsonbCreator(parameters = JsonbCreator.ParameterState.OPTIONAL)
   public Workshop {}
   ```
   
2. Static code analyzers may complain about the code because the constructor body is empty and the canonical constructor is implicitly created. Currently, SonarQube is
   complaining with two code smells (`java:S1186` and `java:S6207`) for the same line with compact constructor.
   
### JsonbConfig
For serialization purposes it is needed to setup configuration so records fields are used to get values instead of accessor methods.

```java
JsonbConfig config = new JsonbConfig()
        .withPropertyVisibilityStrategy(new org.eclipse.yasson.FieldAccessStrategy());     
```

The property visibility strategy was taken from Yasson itself. I think it simplified the example, but I don't recommend this approach. The code depends on the implementation
and that could be the problem if switch to another implementation would be needed in the future.  
There are two possible ways that could solve this problem:
* Future version of JSON-B could detect that class is actually record and use proper strategy by default;
* Common implementations of strategies should be part of spec in the future (currently in [2.x Milestone](https://github.com/eclipse-ee4j/jsonb-api/milestones)) - see
  https://github.com/eclipse-ee4j/jsonb-api/issues/164

## Conclusion
Current state is usable for cases when absent field in JSON can be eliminated. New version of specification should fix this and the situation will be ideal for Java Records as
DTOs. The problem with strategies is minor because we can always create own implementation in our code without referencing Yasson implementation.
