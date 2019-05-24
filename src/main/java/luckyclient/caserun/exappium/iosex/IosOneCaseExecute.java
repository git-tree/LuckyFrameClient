package luckyclient.caserun.exappium.iosex;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import luckyclient.caserun.exappium.AppiumInitialization;
import luckyclient.caserun.exappium.AppiumService;
import luckyclient.caserun.exinterface.TestControl;
import luckyclient.dblog.DbLink;
import luckyclient.dblog.LogOperation;
import luckyclient.publicclass.AppiumConfig;
import luckyclient.publicclass.LogUtil;
import luckyclient.serverapi.api.GetServerAPI;
import luckyclient.serverapi.entity.ProjectCase;
import luckyclient.serverapi.entity.ProjectCaseParams;
import luckyclient.serverapi.entity.ProjectCaseSteps;

/**
 * =================================================================
 * 这是一个受限制的自由软件！您不能在任何未经允许的前提下对程序代码进行修改和用于商业用途；也不允许对程序代码修改后以任何形式任何目的的再发布。
 * 为了尊重作者的劳动成果，LuckyFrame关键版权信息严禁篡改 有任何疑问欢迎联系作者讨论。 QQ:1573584944 seagull1985
 * ================================================================= 
 * @author： seagull 
 * @date 2018年2月2日
 * 
 */
public class IosOneCaseExecute {

	public static void oneCaseExecuteForTast(String projectname, Integer caseId, int version, String taskid)
			throws IOException, InterruptedException {
		// 记录日志到数据库
		DbLink.exetype = 0;
		TestControl.TASKID = taskid;
		IOSDriver<IOSElement> iosd = null;
		AppiumService as=null;
		try {
			Properties properties = AppiumConfig.getConfiguration();
			//根据配置自动启动Appiume服务
			if(Boolean.valueOf(properties.getProperty("autoRunAppiumService"))){
				as =new AppiumService();
				as.start();
				Thread.sleep(10000);
			}
			
			iosd = AppiumInitialization.setIosAppium(properties);
		} catch (IOException e1) {
			LogUtil.APP.error("初始化IOSDriver出错！", e1);
			e1.printStackTrace();
		}
		LogOperation caselog = new LogOperation();
		ProjectCase testcase = GetServerAPI.cGetCaseByCaseId(caseId);
		// 删除旧的日志
		LogOperation.deleteTaskCaseLog(testcase.getCaseId(), taskid);

		List<ProjectCaseParams> pcplist = GetServerAPI.cgetParamsByProjectid(String.valueOf(testcase.getProjectId()));
		LogUtil.APP.info("开始执行用例：【" + testcase.getCaseSign() + "】......");
		try {
			List<ProjectCaseSteps> steps = GetServerAPI.getStepsbycaseid(testcase.getCaseId());
			IosCaseExecution.caseExcution(testcase, steps, taskid, iosd, caselog, pcplist);
			LogUtil.APP.info("当前用例：【" + testcase.getCaseSign() + "】执行完成......进入下一条");
		} catch (InterruptedException e) {
			LogUtil.APP.error("用户执行过程中抛出异常！", e);
			e.printStackTrace();
		}
		LogOperation.updateTaskExecuteData(taskid, 0);
		iosd.closeApp();
		//关闭Appium服务的线程
		if(as!=null){
			as.interrupt();
		}
	}

}
