package hr.aspevec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import hr.aspevec.asm.strategy.services.ASMTestingService;
import hr.aspevec.strategy.services.TestingService;

@Controller
public class TestController {

	@Autowired
	private TestingService testingService;

	@Autowired
	private ASMTestingService asmTestingService;

	@RequestMapping(value="/methodStrategy", method=RequestMethod.GET)
	@ResponseBody
	public TestResponseData methodStrategy() {
		TestResponseData response = new TestResponseData();
		response.setField(testingService.getResult());
		return response;
	}

	@RequestMapping(value="/methodStrategyASM", method=RequestMethod.GET)
	@ResponseBody
	public TestResponseData methodStrategyASM() {
		TestResponseData response = new TestResponseData();
		response.setField(asmTestingService.getResult("ASM"));
		return response;
	}

}
