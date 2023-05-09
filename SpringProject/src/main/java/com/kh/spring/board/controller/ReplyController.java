package com.kh.spring.board.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.kh.spring.board.model.service.ReplyService;
import com.kh.spring.board.model.vo.Reply;

/*
 * Rest(Representation State Transfer)
 *  - 자원을 이름으로 구분하여 자원의 상태를 주고 받는 것
 *  
 *  -> 특정한 이름으로 요청이 오면 "값" 그 자체로 응답
 *  
 * RestController : 요청에 대한 응답이 모두 값 그 자체인 컨트롤러
 *  -> @Controller + @ResponseBody
 */
@RestController
@RequestMapping("/reply")
public class ReplyController {
	
	@Autowired
	private ReplyService replyService;
	
	// 댓글 등록 기능
	@PostMapping("/insert")
	public int insertReply(Reply reply) {
		// ResponseBody 생략되어있음. @RestController 없으면 써야함.
		return replyService.insertReply(reply);
	}
	
	// 댓글 조회 기능
	@GetMapping("/selectReplyList")
	public String selectReplyList(int bno) {
		List<Reply> rList = replyService.selectReplyList(bno);
		return new Gson().toJson(rList);
	}
	
	// 댓글 삭제 기능
	@GetMapping("/delete")
	public int deleteReply(int replyNo) {
		return replyService.deleteReply(replyNo);
	}
	
	// 댓글 수정 기능
	@PostMapping("/update")
	public int updateReply(Reply reply) {
		return replyService.updateReply(reply);
	}
	
}
