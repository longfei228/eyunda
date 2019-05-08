package com.hangyi.eyunda.controller.mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.hangyi.eyunda.chat.data.OnlineUser;
import com.hangyi.eyunda.chat.redis.recorder.OnlineUserRecorder;
import com.hangyi.eyunda.controller.JsonResponser;
import com.hangyi.eyunda.dao.Page;
import com.hangyi.eyunda.data.account.CompanyData;
import com.hangyi.eyunda.data.account.DepartmentData;
import com.hangyi.eyunda.data.account.UserData;
import com.hangyi.eyunda.data.order.OrderTemplateData;
import com.hangyi.eyunda.data.ship.SailLineData;
import com.hangyi.eyunda.data.ship.ShipCabinData;
import com.hangyi.eyunda.data.ship.ShipData;
import com.hangyi.eyunda.data.ship.UpDownPortData;
import com.hangyi.eyunda.domain.enumeric.CargoTypeCode;
import com.hangyi.eyunda.domain.enumeric.OrderTypeCode;
import com.hangyi.eyunda.domain.enumeric.ReleaseStatusCode;
import com.hangyi.eyunda.domain.enumeric.UserPrivilegeCode;
import com.hangyi.eyunda.domain.enumeric.WaresBigTypeCode;
import com.hangyi.eyunda.domain.enumeric.WaresTypeCode;
import com.hangyi.eyunda.service.account.DepartmentService;
import com.hangyi.eyunda.service.order.OrderTemplateService;
import com.hangyi.eyunda.service.portal.login.UserService;
import com.hangyi.eyunda.service.ship.MyCabinService;
import com.hangyi.eyunda.service.ship.MyShipService;
import com.hangyi.eyunda.service.ship.SailLineService;
import com.hangyi.eyunda.service.ship.UpDownPortService;
import com.hangyi.eyunda.util.CalendarUtil;

@Controller
@RequestMapping("/mobile/cabin")
public class MblCabinController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	public static final int PAGE_SIZE = 10;

	@Autowired
	private OnlineUserRecorder onlineUserRecorder;
	@Autowired
	private UserService userService;
	@Autowired
	private OrderTemplateService orderTemplateService;
	@Autowired
	private SailLineService sailLineService;
	@Autowired
	private UpDownPortService upDownPortService;
	@Autowired
	private MyCabinService cabinService;
	@Autowired
	private MyShipService myShipService;
	@Autowired
	private DepartmentService departmentService;

	// 获取当前用户对象，含用户所在公司列表及当前公司
	private UserData getLoginUserData(HttpServletRequest request) throws Exception {
		String sessionId = ServletRequestUtils.getStringParameter(request, MblLoginController.MBL_SESSION_ID, "");
		OnlineUser ou = onlineUserRecorder.getOnlineUser(sessionId);
		if (ou == null)
			throw new Exception("session已丢失请重新登录！");

		UserData userData = userService.getById(ou.getId());
		if (userData == null)
			throw new Exception("未登录不能进行此项操作！");

		String strCompId = ServletRequestUtils.getStringParameter(request, MblLoginController.CURR_COMP_ID, null);
		if (strCompId == null)
			strCompId = "0"; // throw new Exception("你还不属于任何公司，请先注册公司并加入!");
		Long compId = Long.parseLong(strCompId);

		CompanyData currCompanyData = userService.getCompanyData(compId);
		List<CompanyData> companyDatas = userService.getCompanyDatas(userData.getId());
		userData.setCurrCompanyData(currCompanyData);
		userData.setCompanyDatas(companyDatas);

		return userData;
	}

	// 船盘列表
	@RequestMapping(value = "/myCabin", method = { RequestMethod.GET, RequestMethod.POST })
	public void myCabin(Page<ShipCabinData> pageData, Long deptId, String keyWords, String startDate, String endDate,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		try {
			UserData userData = this.getLoginUserData(request);
			String contentMd5 = ServletRequestUtils.getStringParameter(request, JsonResponser.CONTENTMD5, "");

			Map<String, Object> content = new HashMap<String, Object>();

			if (startDate != null && endDate != null && !"".equals(startDate) && !"".equals(endDate)
					&& CalendarUtil.parseYY_MM_DD(endDate).before(CalendarUtil.parseYY_MM_DD(startDate)))
				throw new Exception("错误！起始日期必须小于终止日期。");

			// 船舶组别列表
			List<DepartmentData> departmentDatas = null;
			if (deptId == null)
				deptId = -1L;

			if (userService.isRole(userData, UserPrivilegeCode.handler)) {
				departmentDatas = departmentService.getDepartmentDatas(userData.getCurrCompanyData().getId(),
						UserPrivilegeCode.master);
				pageData = cabinService.getCabinPage(pageData, userData, keyWords, startDate, endDate);
			} else {
				content.put("flag", true);
				content.put("msg", "非业务员不能进行此项操作！");
			}

			// 集装箱规格
			List<CargoTypeCode> containerCodes = new ArrayList<CargoTypeCode>();
			containerCodes.add(CargoTypeCode.container20e);
			containerCodes.add(CargoTypeCode.container20f);
			containerCodes.add(CargoTypeCode.container40e);
			containerCodes.add(CargoTypeCode.container40f);
			containerCodes.add(CargoTypeCode.container45e);
			containerCodes.add(CargoTypeCode.container45f);
			content.put("containerCodes", containerCodes);

			content.put("orderTypes", OrderTypeCode.values());

			// 用户信息
			content.put("userData", userData);

			content.put("departmentDatas", departmentDatas);
			content.put("deptId", deptId);

			content.put("pageData", pageData);
			content.put("keyWords", keyWords);
			content.put("startDate", startDate);
			content.put("endDate", endDate);

			map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
			map.put(JsonResponser.MESSAGE, "获取信息成功");
			map.put(JsonResponser.CONTENT, content);
			map.put(JsonResponser.CONTENTMD5, contentMd5);

		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}
		JsonResponser.respondWithJson(response, map);
	}

	// 添加或修改
	@RequestMapping(value = "/editCabin", method = RequestMethod.GET)
	public void editCabin(Long cabinId, OrderTypeCode orderType, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		try {
			UserData userData = this.getLoginUserData(request);
			UserData broker = userService.getUpData(userData.getCurrCompanyData().getId());

			Map<String, Object> content = new HashMap<String, Object>();

			if (!userService.isRole(userData, UserPrivilegeCode.handler))
				throw new Exception("非业务员不能进行此项操作！");

			ShipCabinData cabinData = cabinService.getById(cabinId);
			if (cabinData == null) {
				cabinData = new ShipCabinData();

				WaresBigTypeCode waresBigType = orderType.getWaresBigType();
				WaresTypeCode waresType = orderType.getWaresType();
				CargoTypeCode cargoType = orderType.getCargoType();

				cabinData.setWaresBigType(waresBigType);
				cabinData.setWaresType(waresType);
				cabinData.setCargoType(cargoType);

				cabinData.setPublisherId(userData.getId());
				cabinData.setBrokerId(broker.getId());
				cabinData.setStatus(ReleaseStatusCode.unpublish);

				if (waresBigType == WaresBigTypeCode.daily) {
					List<UpDownPortData> upDownPortDatas = upDownPortService.getUpDownPortsByType(waresBigType,
							waresType, cargoType);
					cabinData.setUpDownPortDatas(upDownPortDatas);
				}

				if (waresBigType == WaresBigTypeCode.voyage) {
					List<SailLineData> sailLineDatas = sailLineService.getSailLinesByType(waresBigType, waresType,
							cargoType);
					cabinData.setSailLineDatas(sailLineDatas);
				}

				OrderTemplateData otd = orderTemplateService.getTemplateByOrderType(orderType);
				if (otd != null)
					cabinData.setOrderDesc(otd.getOrderContent());// 缺省合同条款
			}

			// 用户信息
			content.put("userData", userData);

			// 船盘信息
			content.put("cabinData", cabinData);

			// 集装箱规格
			List<CargoTypeCode> containerCodes = new ArrayList<CargoTypeCode>();
			containerCodes.add(CargoTypeCode.container20e);
			containerCodes.add(CargoTypeCode.container20f);
			containerCodes.add(CargoTypeCode.container40e);
			containerCodes.add(CargoTypeCode.container40f);
			containerCodes.add(CargoTypeCode.container45e);
			containerCodes.add(CargoTypeCode.container45f);
			content.put("containerCodes", containerCodes);

			map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
			map.put(JsonResponser.MESSAGE, "获取信息成功");
			map.put(JsonResponser.CONTENT, content);
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}
		JsonResponser.respondWithJson(response, map);
	}

	// 保存
	@RequestMapping(value = "/saveCabin", method = { RequestMethod.GET, RequestMethod.POST })
	public void saveCabin(ShipCabinData cabinData, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			UserData userData = this.getLoginUserData(request);

			if (!userService.isRole(userData, UserPrivilegeCode.handler))
				throw new Exception("非业务员不能进行此项操作！");

			WaresBigTypeCode waresBigType = cabinData.getWaresBigType();
			WaresTypeCode waresType = cabinData.getWaresType();
			CargoTypeCode cargoType = cabinData.getCargoType();

			if (waresBigType == WaresBigTypeCode.daily) {
				List<UpDownPortData> spaceUpDownPortDatas = upDownPortService.getUpDownPortsByType(waresBigType,
						waresType, cargoType);

				String ports = "";
				for (UpDownPortData spaceUpDownPortData : spaceUpDownPortDatas) {
					boolean gotoThisPort = ServletRequestUtils.getBooleanParameter(request,
							"gotoThisPort-" + spaceUpDownPortData.getStartPortNo(), false);
					spaceUpDownPortData.setGotoThisPort(gotoThisPort);

					Integer weight = 0;
					if (gotoThisPort) {
						weight = ServletRequestUtils.getIntParameter(request,
								"weight-" + spaceUpDownPortData.getStartPortNo(), 0);
					}
					spaceUpDownPortData.setWeight(weight);

					ports += spaceUpDownPortData.getStartPortNo() + ":" + weight + ";";
				}
				if (!"".equals(ports)) {
					ports = ports.substring(0, ports.length() - 1);
				}
				cabinData.setPorts(ports);
				cabinData.setUpDownPortDatas(spaceUpDownPortDatas);
			}

			if (waresBigType == WaresBigTypeCode.voyage) {
				List<SailLineData> spaceSailLineDatas = sailLineService.getSailLinesByType(waresBigType, waresType,
						cargoType);

				String prices = "";
				for (SailLineData spaceSailLineData : spaceSailLineDatas) {
					boolean gotoThisLine = ServletRequestUtils.getBooleanParameter(request, "gotoThisLine-"
							+ spaceSailLineData.getStartPortNo() + "-" + spaceSailLineData.getEndPortNo(), false);
					spaceSailLineData.setGotoThisLine(gotoThisLine);

					Integer weight = 0;
					if (gotoThisLine) {
						weight = ServletRequestUtils.getIntParameter(request,
								"weight-" + spaceSailLineData.getStartPortNo() + "-" + spaceSailLineData.getEndPortNo(),
								0);
					}
					spaceSailLineData.setWeight(weight);

					Map<Integer, Double> mapPrices = spaceSailLineData.getMapPrices();
					for (Integer key : mapPrices.keySet()) {
						Double price = 0D;
						if (gotoThisLine) {
							price = ServletRequestUtils.getDoubleParameter(request,
									"price-" + spaceSailLineData.getStartPortNo() + "-"
											+ spaceSailLineData.getEndPortNo() + "-" + key,
									0D);
						}
						mapPrices.put(key, price);
					}
					prices += spaceSailLineData.getStartPortData().getPortNo() + "-"
							+ spaceSailLineData.getEndPortData().getPortNo() + ":" + spaceSailLineData.getDistance()
							+ ":" + spaceSailLineData.getWeight() + ":";

					spaceSailLineData.setRemark(spaceSailLineData.makeRemark());
					prices += spaceSailLineData.getRemark() + ";";
				}
				if (!"".equals(prices)) {
					prices = prices.substring(0, prices.length() - 1);
				}
				cabinData.setPrices(prices);
				cabinData.setSailLineDatas(spaceSailLineDatas);
			}

			Long cabinId = cabinService.saveOrUpdate(cabinData, userData);
			if (cabinId > 0) {
				map.put("cabinId", cabinId);
				map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
				map.put(JsonResponser.MESSAGE, "保存成功！");
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "保存失败，没有操作权限！");
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
		return;
	}

	// 发布
	@RequestMapping(value = "/publish", method = { RequestMethod.GET, RequestMethod.POST })
	public void publishCabin(Long cabinId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			UserData userData = this.getLoginUserData(request);

			if (!userService.isRole(userData, UserPrivilegeCode.handler))
				throw new Exception("非业务员不能进行此项操作！");

			if (cabinService.valid(cabinId, userData.getId())) {
				if (cabinService.publish(cabinId)) {
					map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
					map.put(JsonResponser.MESSAGE, "发布成功！");
				} else {
					map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
					map.put(JsonResponser.MESSAGE, "已经发布过了，无需再发布！");
				}
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "没有操作权限,不能发布！");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
		return;
	}

	// 取消发布
	@RequestMapping(value = "/unpublish", method = { RequestMethod.GET, RequestMethod.POST })
	public void unpublishCabin(Long cabinId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			UserData userData = this.getLoginUserData(request);

			if (!userService.isRole(userData, UserPrivilegeCode.handler))
				throw new Exception("非业务员不能进行此项操作！");

			if (cabinService.valid(cabinId, userData.getId())) {
				if (cabinService.unpublish(cabinId)) {
					map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
					map.put(JsonResponser.MESSAGE, "取消发布成功！");
				} else {
					map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
					map.put(JsonResponser.MESSAGE, "未发布，无需取消发布！");
				}
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "没有操作权限,不能取消发布！");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
		return;
	}

	// 删除
	@RequestMapping(value = "/deleteCabin", method = { RequestMethod.GET, RequestMethod.POST })
	public void deleteCabin(Long cabinId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			UserData userData = this.getLoginUserData(request);

			if (!userService.isRole(userData, UserPrivilegeCode.handler))
				throw new Exception("非业务员不能进行此项操作！");

			if (cabinService.valid(cabinId, userData.getId())) {
				cabinService.deleteCabin(cabinId);
				map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
				map.put(JsonResponser.MESSAGE, "已删除！");
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "没有操作权限,不能删除！");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
		return;
	}

	@RequestMapping(value = "/findShip", method = { RequestMethod.GET })
	public void findShip(String keyWords, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			UserData userData = this.getLoginUserData(request);

			Page<ShipData> pageData = new Page<ShipData>();
			if (userService.isRole(userData, UserPrivilegeCode.handler)) {
				pageData = myShipService.getShipPage(userData.getCurrCompanyData().getId(), keyWords,
						pageData.getPageNo(), pageData.getPageSize(), "", 0L, 0L);
			} else {
				throw new Exception("非业务员不能进行此项操作！");
			}

			if (!pageData.getResult().isEmpty()) {
				map.put("content", pageData.getResult());
				map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
				map.put(JsonResponser.MESSAGE, "取得数据成功!");
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "未找到匹配成功的船舶!");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}
		JsonResponser.respondWithJson(response, map);
	}

	// 获取一个船舶对象
	@RequestMapping(value = "/getShip", method = { RequestMethod.GET })
	public void getShip(Long shipId, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			UserData userData = this.getLoginUserData(request);

			if (!userService.isRole(userData, UserPrivilegeCode.handler))
				throw new Exception("非业务员不能进行此项操作！");

			// 处理业务逻辑，为了可以rpc，返回数据必须是序列化的
			ShipData ship = myShipService.getShipData(shipId);
			if (ship != null) {
				map.put("content", ship);
				map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
				map.put(JsonResponser.MESSAGE, "取得数据成功!");
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "取得数据失败!");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, "取得数据失败!");
		}

		JsonResponser.respondWithJson(response, map);
		return;
	}

}