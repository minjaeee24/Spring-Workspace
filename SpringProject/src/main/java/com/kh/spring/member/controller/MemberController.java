package com.kh.spring.member.controller;

import java.util.ArrayList;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.Gson;
import com.kh.spring.member.model.service.MemberService;
import com.kh.spring.member.model.vo.Member;

@Controller // 생성된 bean이 Controller임을 명시 + bean등록
@RequestMapping("/member") // localhost:8081/spring/member 이하의 url요청을 처리하는 컨트롤러

// 로그인과 회원가입기능 완료 후 실행될 코드
@SessionAttributes({"loginUser"})

public class MemberController {

//	public void test() {
//		Member m = new Member().builder().address("추가할 값").userId("dd").build();
//	}

	// private MemberService ms = new MemberServiceImpl();
	// 기존 객체 생성방식. 서비스가 동시에 많은 횟수의 요청이 들어오면 그 만큼의 객체가 생성됨
	// 객체간의 결합도가 올라감.
	
	// Spring의 DI(Dependency Injection) => 객체를 스프링에서 생성해서 주입을 해주는 개념
	// new연산자를 쓰지 않고 필드 선언만 한 후 @Autowired라는 어노테이션을 붙여서 내가 필요로 하는
	// 객체를 스프링 컨테이너로부터 주입받을 수 있음.
	
	/*
	 * AutoWired를 통한 객체 주입방법
	 * 1. 필드방식 의존성 주입
	 * 2. 생성자방식 의존성 주입
	 * 3. setter방식 의존성 주입
	 * 
	 */
	
	/*
	 * 1) 필드방식 의존성
	 * 장점 : 이해하기 편하다, 사용하기 편하다
	 * 
	 * 단점 : 1) 순환 의존성 문제가 발생할 수 있다.
	 * 		 2) 무분별한 주입시 의존관계 확인이 어렵다
	 * 
	 */
	// @Autowired // bean으로 등록된 객체 중 타입이 같거나 상속관계인 bean을 자동으로 주입해주는 역할
	private MemberService memberService;
	
	private BCryptPasswordEncoder bcryptPasswordEncoder; 
	/*
	 * 2) 생성자방식 의존성 주입
	 * 생성자를 통한 의존성 주입 : 생성자에 매개변수로 참조할 클래스를 인자로 받아 필드에 매핑시킴
	 * 
	 * 장점 : 1) 현재 클래스에서 내가 주입시킬 객체들을 모아서 관리할 수 있기 때문에 한 눈에 알아보기 편함
	 * 		 2) 코드분석과 테스트에 유리하며 객체주입시 가장 권장하는 방법
	 * 
	 */
	
//	@Autowired << 생성자 하나면 생략 가능. 여러개면 반드시 추가해야함
	@Autowired
	public MemberController(MemberService memberService, BCryptPasswordEncoder bcryptPasswordEncoder ) {
		this.memberService = memberService;
		this.bcryptPasswordEncoder = bcryptPasswordEncoder; 
	}
	
	public MemberController() {
		
	}
	
	/*
	 * 3) setter방식 의존성 주입
	 * Setter주입방식 : setter메소드로 빈을 주입받는 방식
	 * 
	 * 생성자에 너무 많은 의존성을 주입하게 되면 알아보기 힘들다라는 단점이 있어서 보완하기 위해 사용
	 * 
	 * 필요할때마다 의존성을 주입받아서 사용할때. 즉 의존성주입이 필수가 아닌 선택사항일때 사용
	 * 
	 */
	
	@Autowired
	public void setMemeberService(MemberService memberService) {
		this.memberService = memberService;
	}
	
	/*
	 * 스프링에는 Argument Resolver라는 매개변수를 유연하게 처리해주는 해결사클래스가 내장되어있음
	 * Argument Resolver? 클라이언트가 전달한 파라미터 중 매개변수의 조건과 일치하는 파라미터가 있다면
	 * 해당 객체를 매개변수로 바인딩해준다.
	 * 
	 * 스프링에서 parameter(요청시 전달한 전달값)을 받는 방법
	 * 
	 * 1. HttpServletRequest를 이용해서 전달받기(jsp프로젝트에서 하던 방식 그대로임)
	 * 해당 메소드의 매개변수로 HttpServletRequest를 작성해놓으면 스프링 컨테이너(정확히는 Argument Resolver)가
	 * 해당 메소드를 호출할때 자동으로 request객체를 생성해서 매개변수로 주입해준다
	 */
	
//	@RequestMapping("login")
//	public String loginMember(HttpServletRequest request) {
//		String userId = request.getParameter("userId");
//		String userPwd = request.getParameter("userPwd");
//		
//		System.out.println(userId);
//		System.out.println(userPwd);
//		
//		
//		return "main";
//		
//	}
	
	/*
	 * 2. @RequestParam 어노테이션을 이용하는 방법. request.getParameter("키")로 뽑는 역할을 대신
	 * 수행해주는 어노테이션. jsp에서 작성했던 input속성의 name속성값을 value값으로 입력해주면 알아서 매개변수로 담아온다
	 * 
	 * 만약 넘어온 값이 비어있다면 defaultValue로 기본값 설정 가능
	 * 
	 * 사용 가능 속성 : 
	 * value : input태그의 name속성값으로 다른 속성을 작성하지 않은 경우 기본값으로 활용된다.
	 * 		   @RequestParam("url"), @RequestParam(value="url")
	 * required : 입력된 name속성값이 필수적으로 파라미터에 포함되어야 하는지를 지정(required=true(기본값))
	 * 			  required = true일때 파라미터가 없으면 400에러를 반환(잘못된 요청(bad-request)
	 * 			  required = false일때 파라미터가 없으면 그냥 null값이 들어감
	 * 
	 * defaultValue : required가 false인 상태에서 파라미터가 존재하지 않는 경우의 값을 지정
	 * 
	 */
//	@RequestMapping("/login")
//	public String loginMember(@RequestParam(value="userId", defaultValue="!!!") String userId,
//							  @RequestParam(value="userPwd") String userPwd) {
//		
//		System.out.println(userId);
//		System.out.println(userPwd);
//		
//		return "main";
//	}
	
	/*
	 * 3. @RequestParam 어노테이션을 생략하는 방법
	 * 단, 매개변수명을 jsp의 name속성값(요청시 전달 키값)과 동일하게 작성해줘야한다
	 * 또한, 위에서 작성했던 나머지 속성들 사용 불가
	 * 
	 */
//	@RequestMapping("/login")
//	public String loginMember(String userId, String userPwd) {
//		System.out.println(userId);
//		System.out.println(userPwd);
//		return "main";
//	}
	
	/*
	 * 4. @ModelAttribute 어노테이션을 사용하는 방법.
	 * [작성법]
	 * @ModelAttribute VO타입 변수명
	 * 매개변수로 @ModelAttribute사용시 파라미터 중 name속성 값이 VO클래스의 필드와 일치하면 해당 VO클래스의
	 * 필드에 값을 세팅함
	 * 
	 * [작동원리]
	 * 스프링 컨테이너가 해당 객체를 (기본생성자)로 생성 후 내부적으로 (setter메소드)를 찾아서 요청시 전달할 값 중
	 * name값과 일치하는 필드에 name속성값을 담아줌
	 * 따라서 ModelAttribute를 사용하기 위해선 기본생성자와 + setter메소드는 반드시 필요함
	 * 
	 * @ModelAttribute 생략시 해당 객체를 커맨드객체라고 부른다.
	 * 
	 */
	
//	@RequestMapping("/login")
//	public String loginMember(Member m, Model model) {
//		
//		// 요청처리 후 응답데이터를 담고 응답페이지로 포워딩 또는 url을 재요청하는 방법
//		// 1) Model객체 이용
//		// 포워딩할 응답뷰로 전달하고자 하는 데이터를 map형식으로 담을 수 있는 영역 -> Model객체(requestScope)
//		// Model : 데이터를 맵형식(k:v)형태로 담아 전달하는 용도의 객체
//		// request, session을 대체하는 객체
//		
//		// model클래그 안의 addAttribute()메소드를 이용하는 방법
//		model.addAttribute("errorMsg", "오류발생"); // == request.setAttribute("errorMsg", "오류발생");
//		
//		// Model의 기본 scope는 request scope임. 단 session scope로 변환하고 싶은 경우
//		// 클래스 레벨로 @SessionAttribute를 작성하면 된다
//		model.addAttribute("loginMember", m); // == request.getSession().setAttribute ...
//		
//		System.out.println(m);
//		System.out.println(model);
//		return "main";
//	}
	
	// 2) ModelAndView 객체 이용
	
	// ModelAndView에서 Model은 데이터를 key-value담을 수 있는 Model객체를 의미함
	// View는 응답뷰에 대한 정보를 담을 수 있다. 이때는 리턴타입이 String이 아닌 ModelAndView로 전달해야함
	// Model + View가 결합된 형태의 객체 Model객체는 단독 사용이 가능하지만 View는 불가능함
	// mv.addObject("errorMsg", "로그인실패");
	// mv.setViewName("common/errerPage");
	// return mv;
	
	/*
	 * @RequestMapping? 클라이언트의 요청(url)에 맞는 클래스 or 메소드를 연결시켜주는 어노테이션
	 * 					해당 어노테이션이 붙은 클래스 / 메소드를 스프링 컨테이너가 HandlerMapping으로 등록해준다
	 * 		HandlerMapping? 사용자가 지정한 url정보들을 스프링컨테이너가 모아둔 저장도
	 * 				--> 클래스레벨에서 사용된 경우 : 공통주소로 활용됨(만약 현재 클래스의 공통주소인 member로 요청이
	 * 					들어오면 현재 MemberController가 직접 요청처리를 하게 됨
	 * 				--> 메소드레벨에서 사용된 경우 : 공통주소 외 나머지주소
	 * 		ex) localhost:8081/spring/member(공통주소, 클래스레벨에서 선언) / login(그 외 주소, 메소드레벨에서 선언)
	 * 단, 클래스레벨에 @RequestMapping이 존재하지 않는다면 메소드레벨에서 단독으로 요청을 처리한다(jsp프로젝트에서 하던 방식)
	 * 
	 * [작성법]
	 * 1) @RequestMapping("url") -> url주소만 붙이는 경우 요청방식(get / post)과 관계없이 일치하는 URL에 대해 요청처리한다.
	 * 2) @RequestMapping(value="url", method= RequestMethod.GET / POST) => 일치하는 URL+요청방식을 함께 검사한다.
	 * 
	 * 단, 일반적으로 메소드레벨에서는 GET / POST방식을 구분할때
	 * @GetMapping("url"), @PostMapping("url")을 사용하는게 일반적임
	 */
	@PostMapping("/login")
	public String loginMember(Model model,
								Member m,
								HttpSession session,
								RedirectAttributes ra,
								HttpServletResponse resp,
								HttpServletRequest req,
								@RequestParam(value="seveId", required = false) String saveId) {		
		
//		암호화 전 로그인요청처리
//		Member loginUser = memberService.loginMember(m);
//		
//		if(loginUser == null) {
//			// 실패
//			mv.addObject("errorMsg", "로그인 실패");
//			
//			mv.setViewName("common/errorPage");
//		}else {
//			// 성공
//			session.setAttribute("loginUser", loginUser);
//			mv.setViewName("redirect:/"); // 메인페이지로 url재요청
//			// respinse.sendRedirect(request.getContextPath());
//		}
		
//		암호화 후
		/*
		 * 기존에 평문이 디비에 등록되어있었기 때문에 아이디와 비밀번호를 같이 입력받아 조회하는 형태로 작업하였음
		 * 암호화작업을 하면 입력받은 비밀번호는 평문이지만 디비에 등록된 비밀번호는 암호문이기 때문에 비교시 무조건 다르게 나옴
		 * 아이디로 먼저 회원정보 조회 후 회원이 있으면 비밀번호 암호문 비교 메소드를 이용해서 일치하는지 확인
		 * 
		 */
//		if(true) {
//			throw new RuntimeException(); // 예외 강제 발생			
//		}
		
		Member loginUser = memberService.loginMember(m);
		// loginUser : 아이디 + 비밀번호로 조회한 회원정보 --> 아이디로만 조회
		// loginUser안의 userPwd는 암호화된 비밀번호
		// m안의 userPwd값은 : 암호화되지 않은 평문 비밀번호
		
		// BCryptPasswordEncoder객체의 메소드 중 matches 사용
		// matches(평문, 암호문)을 작성하면 내부적으로 복호화작업이 이루어져서 일치여부를 boolean값으로 반환(true일치, false불일치)
		
		if(loginUser != null && bcryptPasswordEncoder.matches(m.getUserPwd(), loginUser.getUserPwd())) {
			// 두 조건 모두 만족시 로그인 성공
//			mv.addObject("loginUser", loginUser); // MV로 추가시 에러 발생
			model.addAttribute("loginUser", loginUser);
//			session.setAttribute("loginUser", loginUser);
			
			session.setAttribute("alertMsg", "로그인 성공");
			
			// 로그인 성공시 아이디값을 저장하고있는 쿠키 생성(유효시간 1년)
			
			Cookie cookie = new Cookie("saveId", loginUser.getUserId());
			
			if(saveId != null) { // 아이디저장이 체크되었을때
				cookie.setMaxAge(60 * 60 * 24 * 365); // 초단위지정(1년)
			}else { // 아이디저장 체크하지 않았을때
				cookie.setMaxAge(0); // 유효시간 0초 -> 생성되자마자 소멸
			}
			
			// 쿠키 응답시 클라이언트에 전달
			resp.addCookie(cookie);
			
			return "redirect:/";
		}else {
			// 로그인 실패
			ra.addFlashAttribute("errorMsg", "아이디 또는 비밀번호가 일치하지 않습니다");
			// redirect의 특징 -> request에 데이터를 저장할 수 없다.
			// redirect시 잠깐 데이터를 sessionScope에 보관
			// redirect완료 후 다시 requestScope 이관. ==> 페이지 재요청시에도 request스코프에 데이터를 유지 가능
			// : redirect(페이지재요청)시에도 request scope로 세팅된 데이터가 유지될 수 있도록 하는 방법을 spring에서 재공해줌
			//   RedirectAttributes객체(컨트롤러의 매개변수로 작성하면 Argument Resolver가 넣어줌)
			
			return "redirect:/";
		}
		
	}
	
	@GetMapping("/insert") // /spring/member/insert
	public String enrollForm() {
		return "member/memberEnrollForm";
	}
	
	/*
	 * 1. 주입받은 memberService호출해서 insertMember메소드 실행 => db에 새 회원정보 등록
	 * 
	 * 2. 멤버 테이블에 회원가입 등록 성공했다면 alertMsg <-- 회원가입 성공 메세지 보내기 (세션)
	 * 	  멤버 테이블에 회원등록 실패했다면 에러페이지로 메세지 담아 보내기 <-- 회원가입 실패 메세지 (리퀘스트)
	 * 
	 */
	
	@PostMapping("/insert")
	public String insertMember(Member m, HttpSession session, Model model) {
		
		System.out.println("암호화 전 비밀번호 : " + m.getUserPwd());
		
		// 암호화 작업
		String encPwd = bcryptPasswordEncoder.encode(m.getUserPwd());
		
		// 암호화된 pwd를 m의 userPwd다시 대입
		
		m.setUserPwd(encPwd);
		
		System.out.println("암호화 후 비밀번호 : " + m.getUserPwd());
		
		// 1. memberService 호출해서 insertMember메소드 실행 후 db에 회원객체 등록
		int result = memberService.insertMember(m);
		
		/*
		 * 2. 멤버테이블에 회원등록 성공했다면 alertMsg(session)
		 * 					   실패했다면 errorMsg(request)
		 * 
		 */
		String url = "";
		if(result > 0) { // 성공 - 메인페이지로
			session.setAttribute("alertMsg", "회원가입 완료");
			url = "redirect:/";
		}else { // 실패 - 에러페이지로
			model.addAttribute("errorMsg", "회원가입 실패");
			url = "common/errorPage";
		}
		return url; 
	}
	
	@GetMapping("/logout")
	public String logoutMember(HttpSession session, SessionStatus status) {
		
		// 로그아웃기능 ? session안에 저장된 login정보를 날리는게 곧 로그아웃.
		// @SessionAttributes를 이용해서 session scope에 배치된 데이터는 일반적인 방법으로는 없앨 수 없음.
		// SessionStatus라는 별도의 객체를 이용해야만 없앨 수 있다.
		
		// session.invalidate(); // 기존 세션방식으로는 불가능
		status.setComplete(); // 세션 할 일이 완료됨 -> 없앰
		
		return "redirect:/";
		
	}
	
	@ResponseBody // 반환되는 값이 forward/redirect경로가 아닌 값 그 자체임을 의미(ajax시 사용)
	@PostMapping("/selectOne")
	public String selectOne(String input) {
		
		Member m = new Member();
		m.setUserId(input);
		
		Member searchMember = memberService.loginMember(m);
		
		// JSON : 자바스크립트 객체 표기법으로 작성된 문자열형태의 객체.
		
		// GSON 라이브러리 : JSON을 보다 쉽게 다루기 위한 google에서 배포한 라이브러리
		
		return new Gson().toJson(searchMember);
		
	}
	
	@ResponseBody
	@PostMapping("/selectAll")
	public String selectAll() {
		
		ArrayList<Member> selectAll = memberService.selectAll(); 
		
		return new Gson().toJson(selectAll);
	}
	
	/*
	 * 스프링 예외처리 방법(3가지, 중복사용 가능)
	 * 
	 * 1. 메소드별로 예외처리(try / catch, throws)
	 * 
	 * 2. 하나의 컨트롤러에서 발생하는 예외를 싹 모아서 처리 -> @ExceptionHandler
	 * 
	 * 3. 웹 어플리케이션 전역에서 발생하는 예외를 다 모아서 처리 -> @ControllerAdvice
	 * 
	 */
	
	@ExceptionHandler(Exception.class)
	public String exceptionHandler(Exception e, Model model) {
		e.printStackTrace();
		
		model.addAttribute("errorMsg", "서비스 이용중 문제가 발생했습니다.");
		model.addAttribute("e", e);
		
		return "common/errorPage";
	}
	
	// 고정방식(spring-schedler)
	public int count = 0;
	
	@Scheduled(fixedDelay = 1000)
	public void test() {
		// System.out.println("1초마다 출력하기 " + count++);
	}
	
	// crontab방식
	public void testCron() {
		System.out.println("크론 테스트");
	}
	
}
