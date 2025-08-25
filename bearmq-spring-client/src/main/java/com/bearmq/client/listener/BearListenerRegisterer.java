package com.bearmq.client.listener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BearListenerRegisterer implements SmartInitializingSingleton {
  private final ApplicationContext context;
  private final BearerListenerContainer bearerListener;

  public BearListenerRegisterer(
      final ApplicationContext context, final BearerListenerContainer container) {
    this.context = context;
    this.bearerListener = container;
  }

  @Override
  public void afterSingletonsInstantiated() {
    final Map<String, List<Handler>> byQueue = new HashMap<>();

    for (final String name : context.getBeanDefinitionNames()) {
      final Object bean = context.getBean(name);

      for (final Method method : bean.getClass().getMethods()) {
        final BearListener bearAnnotation = method.getAnnotation(BearListener.class);

        if (bearAnnotation == null || method.getParameterCount() > 1) {
          continue;
        }

        for (final String queueName : bearAnnotation.queues()) {
          byQueue.computeIfAbsent(queueName, k -> new ArrayList<>()).add(new Handler(bean, method));
        }
      }
    }

    bearerListener.register(byQueue);
    bearerListener.start();
  }
}
