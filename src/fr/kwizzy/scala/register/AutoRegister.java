package fr.kwizzy.scala.register;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Par Alexis le 20/10/2016.
 */

public class AutoRegister {

    private String packageName;
    private File jar;

    public AutoRegister(String packageName, JavaPlugin instance)
    {
        this.packageName = packageName;
        this.jar = getJar(instance);
    }

    @SuppressWarnings("unchecked")
    public <T> void registerAction(Class<? extends T> mustBePrimary, Consumer<T> action, Class... mustBeOther)
    {
        for (BiValues<? extends Class<?>, Object> objectBiValues : getFinal(mustBePrimary, mustBeOther))
        {
            Bukkit.getLogger().log(Level.INFO, " > Registering: " + objectBiValues.getKey().getSimpleName() + " ...");
            action.accept((T) objectBiValues.getValue());
        }
    }

    private Set<BiValues<? extends Class<?>, Object>> getFinal(Class mustBePrimary, Class... mustBeOther)
    {
        Set<Class<?>> allClasses = getClasses(jar, packageName);
        Set<BiValues<? extends Class<?>, Object>> finalInstance = new HashSet<>();

        Class[] mbp = {mustBePrimary};
        allClasses.stream().filter(aClass -> classContainInterface(aClass, combine(Class.class, mbp, mustBeOther)))
        .filter(aClass -> !hasAnnotation(aClass, DontRegister.class)).forEach(aClass ->
        {
            Object i = getObject(aClass);
            if (i != null)
            {
                BiValues<? extends Class<?>, Object> classObjectPair = new BiValues<>(aClass, i);
                finalInstance.add(classObjectPair);
            }
        });
        return finalInstance;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] combine(Class<T> clazz, T[]...arrays)
    {
        T[] finalArray = (T[]) Array.newInstance(clazz, Arrays.stream(arrays).mapToInt(array -> array.length).sum());
        int i = 0;
        for (T[] array : arrays)
        {
            System.arraycopy(array, 0, finalArray, i, array.length);
            i += array.length;
        }
        return finalArray;
    }

    private File getJar(JavaPlugin javaPlugin)
    {
        Class aClass = JavaPlugin.class;

        Object invoke = null;
        try
        {
            Field getFile = aClass.getDeclaredField("file");
            getFile.setAccessible(true);
            invoke = getFile.get(javaPlugin);
        } catch (Exception e)
        {
            printException(e);
        }
        return (File) invoke;
    }

    private boolean classContainInterface(Class c, Class... interfaces)
    {
        int checked = 0;

        for (Class anInterface : interfaces)
        {
            for (Class aClass : c.getInterfaces())
            {
                if(classEqual(aClass, anInterface))
                    checked++;
            }
        }
        return checked == interfaces.length;
    }

    private Object getObject(Class c)
    {
        try
        {
            Object[] objs = {getInstanceFromField(c), getInstanceFromMethod(c)};
            if(objs[0] != null)
                return objs[0];
            else if(objs[1] != null)
                return objs[1];

            for (Constructor constructor : c.getDeclaredConstructors())
            {
                int parameterCount = constructor.getParameterCount();
                constructor.setAccessible(true);
                if(parameterCount == 0 && constructor.isAccessible())
                        return constructor.newInstance();

            }
        } catch (ReflectiveOperationException e)
        {
            printException(e);
        }
        return null;
    }

    private Object getInstanceFromField(Class c)
    {
        try
        {
            for (Field field : c.getFields())
            {
                if(hasAnnotation(field, RegisterInstance.class))
                {
                    return field.get(null);
                }
            }
        } catch (ReflectiveOperationException e)
        {
            printException(e);
        }
        return null;
    }

    private Object getInstanceFromMethod(Class c)
    {
        try
        {
            for (Method m : c.getMethods())
            {
                m.setAccessible(true);
                if(hasAnnotation(m, RegisterInstance.class) && m.getParameterCount() == 0)
                    return m.invoke(null);
            }
        } catch (ReflectiveOperationException e)
        {
            printException(e);
        }
        return null;
    }

    private boolean classEqual(Class c, Class f)
    {
        String one = c.getName();
        String two = f.getName();
        return one.equals(two);
    }

    private boolean hasAnnotation(AnnotatedElement m, Class<? extends Annotation> c)
    {
        for (Annotation annotation : m.getAnnotations())
        {
            if(classEqual(annotation.getClass(), c))
               return true;

        }
        return false;
    }

    private Set<Class<?>> getClasses(File jarFile, String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        try (JarFile file = new JarFile(jarFile)){
            for (Enumeration<JarEntry> entry = file.entries(); entry.hasMoreElements();) {
                JarEntry jarEntry = entry.nextElement();
                String name = jarEntry.getName().replace("/", ".");
                if(name.startsWith(packageName) && name.endsWith(".class"))
                    classes.add(Class.forName(name.substring(0, name.length() - 6)));
            }
            file.close();
        } catch(Exception e) {
            printException(e);
        }
        return classes;
    }

    private void printException(Exception e)
    {
        Bukkit.getLogger().log(Level.SEVERE, "Error when use methods in " + AutoRegister.class.getSimpleName() + " :");
        Bukkit.getLogger().log(Level.SEVERE, "  Cause : " + e.getCause());
    }

    public static void registerEvents(String packageName, JavaPlugin instance)
    {
        new AutoRegister(packageName, instance).registerAction(Listener.class,
                t -> Bukkit.getPluginManager().registerEvents(t, instance));
    }

    public static void registerCommands(String packageName, JavaPlugin instance)
    {
        new AutoRegister(packageName, instance).registerAction(CommandExecutor.class,
                t -> instance.getCommand(((CommandInfo) t).getCommandName()).setExecutor(t), CommandInfo.class);
    }

    private class BiValues<K, V> {
        K t;
        V v;

        BiValues(K t, V v)
        {
            this.t = t;
            this.v = v;
        }

        K getKey()
        {
            return t;
        }

        V getValue()
        {
            return v;
        }
    }

}
