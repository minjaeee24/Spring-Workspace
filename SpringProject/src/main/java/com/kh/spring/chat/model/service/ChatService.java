package com.kh.spring.chat.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.spring.chat.model.vo.ChatRoom;

public interface ChatService {

	List<ChatRoom> selectChatRoomList();
	
}
