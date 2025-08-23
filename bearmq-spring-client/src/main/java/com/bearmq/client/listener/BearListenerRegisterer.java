package com.bearmq.client.listener;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BearListenerRegisterer implements SmartInitializingSingleton {
  private final ApplicationContext applicationContext;
  private final BearerListenerContainer bearerListenerContainer;

  public BearListenerRegisterer(final ApplicationContext context, final BearerListenerContainer container) {
    this.applicationContext = context;
    this.bearerListenerContainer = container;
  }

  @Override
  public void afterSingletonsInstantiated() {
    final Map<String, List<Handler>> byQueue = new HashMap<>();

    for (final String name : applicationContext.getBeanDefinitionNames()) {
      final Object bean = applicationContext.getBean(name);

      for (Method m : bean.getClass().getMethods()) {
        final BearListener bearAnnotation = m.getAnnotation(BearListener.class);

        if (bearAnnotation == null || m.getParameterCount() > 1) {
          continue;
        }

        for (final String q : bearAnnotation.queues()) {
          byQueue.computeIfAbsent(q, k -> new ArrayList<>()).add(new Handler(bean, m));
        }
      }
    }

    bearerListenerContainer.register(byQueue);
    bearerListenerContainer.start();
  }
}
