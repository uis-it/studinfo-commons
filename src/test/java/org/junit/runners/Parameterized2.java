/*
 Copyright 2010-2013 University of Stavanger, Norway

 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
// CHECKSTYLE:OFF
package org.junit.runners;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * <p>
 * The custom runner <code>Parameterized</code> implements parameterized tests.
 * When running a parameterized test class, instances are created for the
 * cross-product of the test methods and the test data elements.
 * </p>
 * 
 * For example, to test a Fibonacci function, write:
 * 
 * <pre>
 * &#064;RunWith(Parameterized.class)
 * public class FibonacciTest {
 *  &#064;Parameters
 *  public static List&lt;Object[]&gt; data() {
 *    return Arrays.asList(new Object[][] {
 *      { 0, 0 }, { 1, 1 }, { 2, 1 }, { 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 }
 *    });
 *  }
 * 
 *  private int fInput;
 * 
 *  private int fExpected;
 * 
 *  public FibonacciTest(int input, int expected) {
 *    fInput= input;
 *    fExpected= expected;
 *  }
 * 
 *  &#064;Test
 *  public void test() {
 *    assertEquals(fExpected, Fibonacci.compute(fInput));
 *  }
 * }
 * </pre>
 * 
 * <p>
 * Each instance of <code>FibonacciTest</code> will be constructed using the
 * two-argument constructor and the data values in the
 * <code>&#064;Parameters</code> method.
 * </p>
 */
public class Parameterized2 extends Suite {
  
  /**
   * Annotation for a method which provides parameters to be injected into the
   * test class constructor by <code>Parameterized</code>.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Parameters {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface TestName {
  }
  
  private class TestClassRunnerForParameters extends BlockJUnit4ClassRunner {
    private final int fParameterSetNumber;

    private final List<Object[]> fParameterList;

    TestClassRunnerForParameters(Class<?> type, List<Object[]> parameterList, int i) throws InitializationError {
      super(type);
      fParameterList = parameterList;
      fParameterSetNumber = i;
    }

    @Override
    public Object createTest() throws Exception {
      return getTestClass().getOnlyConstructor().newInstance(
          computeParams());
    }

    private Object[] computeParams() throws Exception {
      try {
        return fParameterList.get(fParameterSetNumber);
      } catch (ClassCastException e) {
        throw new Exception(String.format(
            "%s.%s() must return a Collection of arrays.",
            getTestClass().getName(), getParametersMethod(
                getTestClass()).getName()));
      }
    }

    @Override
    protected String getName() {
      return Arrays.toString(fParameterList.get(fParameterSetNumber));
    }

    @Override
    protected String testName(final FrameworkMethod method) {
      
      try {
        String description = getTestName(getTestClass(), method.getName(), fParameterList.get(fParameterSetNumber));
        if (description != null) {
          return description;
        }
      } catch(Throwable e) {
        // ignored
      }
      return String.format("%s[%s]", method.getName(),
          fParameterSetNumber);
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
      validateOnlyOneConstructor(errors);
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
      return childrenInvoker(notifier);
    }
    
    @Override
    protected Annotation[] getRunnerAnnotations() {
      return new Annotation[0];
    }
  }

  /**
   * Only called reflectively. Do not use programmatically.
   */
  public Parameterized2(Class<?> klass) throws Throwable {
    super(klass, new ArrayList<Runner>());
    List<Object[]> parametersList = getParametersList(getTestClass());
    List<Runner> runners = getChildren();
    for (int i = 0; i < parametersList.size(); i++) {
      
      runners.add(new TestClassRunnerForParameters(getTestClass().getJavaClass(),
          parametersList, i));
    }
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> getParametersList(TestClass klass) throws Throwable {
    return (List<Object[]>) getParametersMethod(klass).invokeExplosively(
        null);
  }

  private FrameworkMethod getFrameworkMethod(TestClass testClass, Class<? extends Annotation> annotationClass)
      throws Exception 
  {
    List<FrameworkMethod> methods = testClass.getAnnotatedMethods(annotationClass);
    for (FrameworkMethod method : methods) {
      int modifiers = method.getMethod().getModifiers();
      if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
        return method;
      }
    }

    throw new Exception("No public static method annotated with " + annotationClass.getName() + " on class "
        + testClass.getName());
  }
  
  private String getTestName(TestClass klass, String methodName, Object[] params) throws Throwable {
    return (String)getDescriptionsMethod(klass).invokeExplosively(null, methodName, params);
  }
  
  private FrameworkMethod getParametersMethod(TestClass testClass)
      throws Exception 
  {
    return getFrameworkMethod(testClass, Parameters.class);
  }
  
  private FrameworkMethod getDescriptionsMethod(TestClass testClass) throws Exception {
    try {
      return getFrameworkMethod(testClass, TestName.class);
    } catch(Exception ex) {
    }
    return null;
  }
}
