# Read me

The original idea is from  
https://medium.com/@danylo.liakh/using-component-and-stepscope-together-in-java-spring-batch-best-practices-8ba8b0b040a9

# StepScope

Using @Component and @StepScope together is acceptable.

Unlike a singleton bean, a step-scoped bean is instantiated each time a step is executed. This allows it to access
resources like job parameters and step-specific data.