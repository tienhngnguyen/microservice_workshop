package com.metamagic.ms;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamagic.ms.bean.Order;
import com.metamagic.ms.events.OrderPlacedEvent;
import com.metamagic.ms.service.write.OrderWriteService;

@Component
public class MessageReceiver {

	  @Autowired
	  private OrderWriteService orderWriteService;
	
	  private CountDownLatch latch = new CountDownLatch(1);

	  public CountDownLatch getLatch() {
	    return latch;
	  }

	  @KafkaListener(topics = "test_topic")
	  public void receive(String msg) throws JsonParseException, JsonMappingException, IOException {
	    System.out.println(msg);
	    latch.countDown();
	    
	    ObjectMapper mapper = new ObjectMapper();
	    OrderPlacedEvent orderPlacedEvent = mapper.readValue(msg, OrderPlacedEvent.class);
	    double total = 0.0;
	    if(orderPlacedEvent.getItems()!=null) {
	    	total = orderPlacedEvent.getItems().stream().mapToDouble(o -> o.getPrice()).sum();
	    }
	    Order order = new Order(orderPlacedEvent.getCartId(), orderPlacedEvent.getCustomerId(), new Date(), orderPlacedEvent.getItems(), total, "COMPLETED");
	    orderWriteService.save(order);
	  }
}
