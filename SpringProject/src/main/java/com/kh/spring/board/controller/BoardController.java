package com.kh.spring.board.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kh.spring.board.model.service.BoardService;
import com.kh.spring.board.model.vo.Board;
import com.kh.spring.member.model.vo.Member;

@Controller
@RequestMapping("/board")
public class BoardController{
	
	@Autowired
	private BoardService boardService;
	
	/*
	 * 게시글목록 조회 서비스
	 * 
	 * @PathVariable : URL경로에 포함되어있는 값을 변수로 사용할 수 있게 하는 역할
	 * => ★자동으로 request scope에 등록된다. ==> jsp에서 ${value} el로 작성가능하다.
	 * 
	 * PathVariable : 요청 자원을 식별하는 경우에 사용
	 * 
	 * QueryString : 정렬, 검색 등 필터링옵션이 있을 경우 사용
	 * 
	 */
	@GetMapping("/list/{boardCode}")
	public String boardList(@PathVariable("boardCode") String boardCode, 
							@RequestParam(value = "cpage", defaultValue = "1") int currentPage,
							Model model,
							@RequestParam Map<String, Object> paramMap) {
		// 검색 요청이 있었다면, paramMap안에는 keyword, condition이 들어가있을것.
		System.out.println(paramMap);
		Map<String, Object> map = new HashMap(); 
		// 게시글 목록 조회 서비스 호출 시 작업 내용
		// 1) 게시판 이름 조회
		// System.out.println(map);
		if(paramMap.get("condition") == null) {
			// 검색 요청을 하지 않은 경우
			System.out.println(map);
			boardService.selectBoardList(currentPage, boardCode, map);
		}else {
			// 검색 요청을 한 경우
			// 검색 조건을 추가한 상태로 게시글 셀렉트
			map.put("condition", paramMap.get("condition"));
			map.put("keyword", "%"+paramMap.get("keyword")+"%");
			map.put("boardCode", boardCode);
			System.out.println(map);
			boardService.selectSearchBoardList(currentPage, boardCode, map);
		}
		
		// model.addAttribute("list", list);
		// model.addAttribute("pi", pi);
		model.addAttribute("map", map);
		
		return "board/boardListView";
		
	}
	
	// 게시글 상세 조회
	@GetMapping("/detail/{boardCode}/{boardNo}")
	public String boardDetail(@PathVariable("boardCode") String boardCode,
							  @PathVariable("boardNo") int boardNo,
							  @RequestParam(value="cpage", required = false, defaultValue="1") int cp,
							  Model model,
							  HttpSession session,
							  HttpServletRequest req,
							  HttpServletResponse resp) {
		
		// 게시글 상세 조회 서비스 호출
		Board detail = boardService.selectBoardDetail(boardNo);
		
		// 조회수 중복 증가 방지 코드(본인 글은 조회수 증가 안 되게) -> 쿠키를 활용할 예정
		
		if(detail != null) { // 상세조회 성공
			Member loginUser = (Member)session.getAttribute("loginUser");
			
			int memberNo = 0;
			
			if(loginUser != null) { // 로그인 한 상태일때
				memberNo = loginUser.getUserNo();
			}
			
			// 글쓴이와 현재 상세보기요청을 한 클라이언트가 같지 않은 경우에만 조회수 증가서비스 호출
			if(Integer.parseInt(detail.getBoardWriter()) != memberNo) {
				Cookie cookie = null;
				
				Cookie[] cArr = req.getCookies(); // 쿠키 열어 보기
				
				if(cArr != null && cArr.length > 0) { // 열어온 쿠키가 있을 경우
					for(Cookie c : cArr) {
						if(c.getName().equals("readBoardNo")) {
							cookie = c;
						}
					}
				}
				int result = 0;
				if(cookie == null) { // 기존엔 readBoardNo라는 이름의 쿠키가 없던 경우
					cookie = new Cookie("readBoardNo", boardNo + "");
					result = boardService.updateReadCount(boardNo); // 조회수 증가서비스 호출
				}else {
					// readBoardNo가 이미 존재할 경우 -> 쿠키에 저강된 값 뒤쪽에 현재 조회된 게시글 번호를 추가
					//						 	 	 단, 기존 쿠키값중에 중복되는 번호가 없어야한다.
					String temp[] = cookie.getValue().split("/");
					//"readBoardNo" / "1/2/3/4/5/6/7/8/9/100"
					
					java.util.List<String> list = Arrays.asList(temp); // 배열 --> List로 변환시켜주는 함수
					
					
					// List.indexOf(Object) : 
					// - List에서 Object와 일치하는 부분의 인덱스를 변환
					// - 일치하는 부분이 없으면 -1을 반환
					if(list.indexOf(boardNo + "") == -1) { // 즉 기존값에 같은 글번호가 없다면
						cookie.setValue(cookie.getValue() + "/" + boardNo);
						result = boardService.updateReadCount(boardNo); // 조회수 증가서비스 호출
					}
					
				}
				if(result > 0) {
					
					cookie.setPath(req.getContextPath());
					cookie.setMaxAge(60 * 60 * 1); // 1시간
					resp.addCookie(cookie);
				}
			}
		}
		model.addAttribute("b", detail);
		return "board/boardDetailView";
	}
	
}
	