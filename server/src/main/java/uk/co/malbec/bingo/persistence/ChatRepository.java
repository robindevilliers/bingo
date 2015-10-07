package uk.co.malbec.bingo.persistence;

import org.springframework.stereotype.Repository;
import uk.co.malbec.bingo.model.ChatMessage;

import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Repository
public class ChatRepository {

    private final Map<String, Deque<ChatMessage>> chatRooms = new HashMap<>();
    private Map<String, Integer> nextIndexes = new HashMap<>();

    public List<ChatMessage> getChatMessagesAfterIndex(String chatRoom, int messageIndex) {

        Deque<ChatMessage> messages;
        synchronized (chatRooms) {
            messages = ofNullable(chatRooms.get(chatRoom)).orElseGet(() -> initialiseChatRoom(chatRoom));
        }

        synchronized (messages) {
            return messages
                    .stream()
                    .filter(chatMessage -> chatMessage.getMessageIndex() > messageIndex)
                    .collect(toList());
        }
    }

    public void addMessage(String chatRoom, ChatMessage chatMessage) {

        Deque<ChatMessage> messages;
        synchronized (chatRooms) {
            messages = ofNullable(chatRooms.get(chatRoom)).orElseGet(() -> initialiseChatRoom(chatRoom));
        }

        synchronized (messages) {

            if (messages.size() == 100) {
                messages.removeLast();
            }

            synchronized (nextIndexes) {
                chatMessage.setMessageIndex(nextIndexes.get(chatRoom));
                nextIndexes.put(chatRoom, nextIndexes.get(chatRoom) + 1);
            }

            messages.addFirst(chatMessage);
        }
    }

    private Deque<ChatMessage> initialiseChatRoom(String chatRoom) {
        synchronized (nextIndexes){
            nextIndexes.put(chatRoom, 1);
        }
        Deque<ChatMessage> messages = new LinkedList<>();
        chatRooms.put(chatRoom, messages);
        return messages;
    }
}
