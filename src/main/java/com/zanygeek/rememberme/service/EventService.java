package com.zanygeek.rememberme.service;

import com.zanygeek.rememberme.entity.Event;
import com.zanygeek.rememberme.entity.Map;
import com.zanygeek.rememberme.entity.XY;
import com.zanygeek.rememberme.repository.EventRepository;
import com.zanygeek.rememberme.repository.MapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {
    @Autowired
    EventRepository eventRepository;

    public Event saveEvent(Event event, int memorialId) {
        event.setMemorialId(memorialId);
        event.setText(event.getText().replaceAll("\r\n", "<br>"));
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(int eventId,int memorialId){
       Event savedEvent = eventRepository.getById(eventId);
       if(savedEvent.getMemorialId()==memorialId)
           eventRepository.delete(savedEvent);
    }

    public List<Event> getEvents(int memorialId) {
        return eventRepository.findAllByMemorialIdOrderByDateDesc(memorialId);
    }
}
