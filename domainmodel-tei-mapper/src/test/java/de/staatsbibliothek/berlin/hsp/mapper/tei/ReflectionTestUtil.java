package de.staatsbibliothek.berlin.hsp.mapper.tei;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ReflectionTestUtil {

  private ReflectionTestUtil() {
    // Utility class
  }

  /**
   * Finds all subclasses of the given superclass within the specified package.
   *
   * @param superclass  the superclass to find subclasses of
   * @param packageName the package to scan
   * @return a list of all subclasses found
   */
  public static List<Class<?>> findAllSubclasses(Class<?> superclass, String packageName) {
    List<Class<?>> subclasses = new ArrayList<>();
    String path = packageName.replace('.', '/');

    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Enumeration<URL> resources = classLoader.getResources(path);

      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        File directory = new File(resource.toURI());

        if (directory.exists()) {
          scanDirectory(directory, packageName, superclass, subclasses);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to scan package: " + packageName, e);
    }

    return subclasses;
  }

  private static void scanDirectory(File directory, String packageName, Class<?> superclass,
      List<Class<?>> subclasses) {
    File[] files = directory.listFiles();
    if (files == null) {
      return;
    }

    for (File file : files) {
      if (file.isDirectory()) {
        scanDirectory(file, packageName + "." + file.getName(), superclass, subclasses);
      } else if (file.getName().endsWith(".class")) {
        String className = packageName + '.' +
            file.getName().substring(0, file.getName().length() - 6);

        try {
          Class<?> clazz = Class.forName(className);
          if (superclass.isAssignableFrom(clazz) && !clazz.equals(superclass)
              && !clazz.isInterface()) {
            subclasses.add(clazz);
          }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
          // Skip classes that can't be loaded
        }
      }
    }
  }
}