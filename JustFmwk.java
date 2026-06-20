import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;


public class JustFmwk {

    private static Map<String, Object> beanRegistry = new HashMap<>();

    public static void setupApplication(Class<?> mainClass) {
        String rootPackageName = mainClass.getPackage().getName();
        String programRootPath = convertPackageToPath(rootPackageName);
       
        InputStream s = ClassLoader.getSystemClassLoader().getResourceAsStream(programRootPath);
        BufferedReader r = new BufferedReader(new InputStreamReader(s));

        Set<Class<?>> classSet = r.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> {
                    try {
                        return Class.forName(line.substring(0, line.lastIndexOf(".")));
                    } catch (ClassNotFoundException e) {
                        System.out.println("Something went wrong: " + e.getMessage());
                    }
                    return null;
                })
                .collect(Collectors.toSet());


        classSet.forEach(element -> {
            try {
                if (element.getSimpleName().equals(mainClass.getSimpleName())) {
                    Field[] fields = element.getDeclaredFields();

                    for (Field f : fields) {
                        if (f.isAnnotationPresent(Inject.class)) {
                            Object fieldInstance = getOrCreateAndRegisterNewInstance(f.getType());                        
                            f.setAccessible(true);
                            f.set(element, fieldInstance);
                        }
                    }
                    return;
                }

                if (element.isAnnotationPresent(Component.class)) {

                    Object elementInstance = getOrCreateAndRegisterNewInstance(element);   

                    Field[] fields = element.getDeclaredFields();

                    for (Field f : fields) {
                        if (f.isAnnotationPresent(Inject.class)) {
                            Object fieldInstance = getOrCreateAndRegisterNewInstance(f.getType());                        
                            f.setAccessible(true);
                            f.set(elementInstance, fieldInstance);
                        }
                    }
                }

            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                System.out.println("Something happened, that's all I know, here's message:  " + e.getMessage());
            }
        });
    }

    private static String convertPackageToPath(String packageName) {
        String programRootPath = packageName.replace(".", "/");
        return programRootPath;
    }

    private static Object getOrCreateAndRegisterNewInstance(Class<?> obj) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{        

        Object existingObject = beanRegistry.get(obj.getSimpleName());

        if(existingObject != null)
            return existingObject;
        
        Object objectInstance = obj.getDeclaredConstructor().newInstance();
        beanRegistry.put(obj.getSimpleName(), objectInstance);

        return objectInstance;
    }
}

@Retention(RetentionPolicy.RUNTIME)
@interface Inject {
}

@Retention(RetentionPolicy.RUNTIME)
@interface Component {
}