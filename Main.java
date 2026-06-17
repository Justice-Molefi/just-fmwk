import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;

public class Main {

    @AutoWired
    private static OrderService orderService;
    public static void main(String[] args) throws NoSuchMethodException {
        setupApplication();
        orderService.userService.createUser();
    }

    private static void setupApplication(){
          InputStream s = ClassLoader.getSystemClassLoader().getResourceAsStream(" ");

        BufferedReader r = new BufferedReader(new InputStreamReader(s));

        Set<Class<?>> classSet = r.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> {
                    try {
                        return Class.forName(line.substring(0, line.lastIndexOf(".")));
                    } catch (ClassNotFoundException e) {
                        System.out.println("Something happened " + e.getMessage());
                    }
                    return null;
                })
                .collect(Collectors.toSet());



        Map<String, Object> beanRegistry = new HashMap<>();
        classSet.forEach(element -> {
             try {

                //lay and pray that main Method is inside Main class lol....
                if(element.getSimpleName().equals("Main")){
                    Field [] fields = element.getDeclaredFields();

                    for(Field f : fields){
                        if (f.isAnnotationPresent(AutoWired.class)) {
                            Object fieldInstance = f.getType().getDeclaredConstructor().newInstance();
                            String fieldTypeName = f.getType().getSimpleName();
                            f.set(element, fieldInstance);
                            beanRegistry.put(fieldTypeName, fieldInstance);
                        }
                    }
                    return;
                }

                
                if (element.isAnnotationPresent(Component.class)) {

                    Object elementInstance; 
                    Object existingBean = beanRegistry.get(element.getSimpleName());

                    if(existingBean == null ){
                        elementInstance = element.getDeclaredConstructor().newInstance();
                        beanRegistry.put(element.getSimpleName(), elementInstance);
                    }else{
                        elementInstance = existingBean;
                    }

                    Field[] fields = element.getDeclaredFields();

                    for (Field f : fields) {
                        if (f.isAnnotationPresent(AutoWired.class)) {
                            Object fieldInstance = f.getType().getDeclaredConstructor().newInstance();
                            String fieldTypeName = f.getType().getSimpleName();
                            f.set(elementInstance, fieldInstance);
                            beanRegistry.put(fieldTypeName, fieldInstance);
                        }
                    }
                }

            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                System.out.println("Something happened, that's all I know, here's message:  " + e.getMessage());
            }
        });

    }

}






@Component
class UserService {
    public void createUser() {
        System.out.println("Creating user");
    }
}


@Component
class OrderService {
    @AutoWired
    public UserService userService;
}

@Retention(RetentionPolicy.RUNTIME)
@interface AutoWired {
}

@Retention(RetentionPolicy.RUNTIME)
@interface Component {
}