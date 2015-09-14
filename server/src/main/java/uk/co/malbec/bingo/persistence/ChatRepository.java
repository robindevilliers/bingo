package uk.co.malbec.bingo.persistence;

import org.springframework.stereotype.Repository;
import uk.co.malbec.bingo.model.ChatMessage;

import java.util.*;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Repository
public class ChatRepository {

    private Map<String, Deque<ChatMessage>> chatRooms = new HashMap<>();
    private Map<String, Integer> nextIndexes = new HashMap<>();

    public List<ChatMessage> getChatMessagesAfterIndex(String chatRoom, int messageIndex) {

        Deque<ChatMessage> messages =
                ofNullable(chatRooms.get(chatRoom))
                        .orElseGet(() -> initialiseChatRoom(chatRoom));

        return messages
                .stream()
                .filter(chatMessage -> chatMessage.getMessageIndex() > messageIndex)
                .collect(toList());
    }

    public void addMessage(String chatRoom, ChatMessage chatMessage) {
        Deque<ChatMessage> messages = ofNullable(chatRooms.get(chatRoom)).orElseGet(() -> initialiseChatRoom(chatRoom));

        if (messages.size() == 100) {
            messages.removeLast();
        }

        chatMessage.setMessageIndex(nextIndexes.get(chatRoom));
        nextIndexes.put(chatRoom, nextIndexes.get(chatRoom) + 1);

        messages.add(chatMessage);
    }

    private Deque<ChatMessage> initialiseChatRoom(String chatRoom) {
        nextIndexes.put(chatRoom, 1);
        Deque<ChatMessage> messages = new LinkedList<>();
        chatRooms.put(chatRoom, messages);
        return messages;
    }
}
