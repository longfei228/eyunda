package com.hangyi.eyunda.controller.hyquan.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.hangyi.eyunda.controller.JsonResponser;
import com.hangyi.eyunda.controller.hyquan.HyqBaseController;
import com.hangyi.eyunda.data.chat.ChatMessageData;
import com.hangyi.eyunda.data.chat.ChatRoomData;
import com.hangyi.eyunda.data.hyquan.HyqDepartmentData;
import com.hangyi.eyunda.data.hyquan.HyqUserData;
import com.hangyi.eyunda.data.hyquan.HyqWalletData;
import com.hangyi.eyunda.domain.enumeric.PayStatusCode;
import com.hangyi.eyunda.service.chat.ChatMessageService;
import com.hangyi.eyunda.service.hyquan.HyqDepartmentService;
import com.hangyi.eyunda.service.hyquan.HyqUserService;
import com.hangyi.eyunda.service.hyquan.chat.HyqBonusService;
import com.hangyi.eyunda.service.hyquan.chat.HyqChatMessageService;
import com.hangyi.eyunda.service.hyquan.chat.HyqChatRoomService;
import com.hangyi.eyunda.service.hyquan.wallet.HyqWalletService;
import com.hangyi.eyunda.util.Constants;

@Controller
@RequestMapping("/hyquan/chat")
public class HyqChatController extends HyqBaseController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HyqDepartmentService departmentService;
	@Autowired
	private HyqUserService userService;
	@Autowired
	private HyqChatRoomService chatRoomService;
	@Autowired
	private HyqChatMessageService chatMessageService;
	@Autowired
	private HyqBonusService bonusService;
	@Autowired
	private HyqWalletService walletBillService;

	// 取得聊天室列表
	@RequestMapping(value = "/getChatRooms", method = RequestMethod.GET)
	public void getChatRooms(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> content = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			// 取得我的聊天室列表
			List<ChatRoomData> myRoomDatas = chatRoomService.getMyChatRoomDatas(selfData.getId());
			content.put("myRoomDatas", myRoomDatas);

			map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
			map.put(JsonResponser.MESSAGE, "成功");
			map.put(JsonResponser.CONTENT, content);
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}
		JsonResponser.respondWithJson(response, map);
	}

	// 取得一个聊天室、成员、以及1-n页聊天记录
	@SuppressWarnings("static-access")
	@RequestMapping(value = "/openChatRoom", method = RequestMethod.GET)
	public void openChatRoom(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> content = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			// 当前聊天室roomId
			Long roomId = ServletRequestUtils.getLongParameter(request, "roomId", 0L);

			// 取得聊天室，含统计未读记录数，若放前面就不对了
			ChatRoomData openedChatRoom = chatRoomService.getChatRoomData(roomId, selfData.getId());
			// 当前聊天室toUserId
			if (openedChatRoom == null) {
				Long toUserId = ServletRequestUtils.getLongParameter(request, "toUserId", 0L);
				HyqUserData toUserData = userService.getById(toUserId);

				if (toUserData == null)
					throw new Exception("聊天对象不存在，无法进入聊天室!");
				if (selfData.getId().equals(toUserId))
					throw new Exception("自己不应该与自己聊天，无法进入聊天室!");

				openedChatRoom = chatRoomService.getByTwoUserId(selfData.getId(), toUserId);
				if (openedChatRoom == null) {
					openedChatRoom = new ChatRoomData();

					roomId = chatRoomService.saveChatRoom(openedChatRoom, new Long[] { selfData.getId(), toUserId });
					openedChatRoom = chatRoomService.getChatRoomData(roomId, selfData.getId());
				}
			}
			content.put("openedChatRoom", openedChatRoom);

			// 取得聊天室成员列表
			List<HyqUserData> chatRoomMembers = chatRoomService.getRoomMembers(roomId);
			content.put("chatRoomMembers", chatRoomMembers);

			// 取得1-n页消息列表
			List<ChatMessageData> chatRoomMessages = new ArrayList<ChatMessageData>();
			Integer pageNo = ServletRequestUtils.getIntParameter(request, "pageNo", 1);
			List<ChatMessageData> cmds = chatMessageService.getPageChatMsgDatas(ChatMessageService.CHAT_PAGESIZE,
					pageNo, openedChatRoom.getId(), selfData.getId());
			chatRoomMessages.addAll(cmds);

			Collections.reverse(chatRoomMessages);
			content.put("chatRoomMessages", chatRoomMessages);
			boolean isHaveNextPage = false;
			if (chatRoomMessages.size() == chatMessageService.CHAT_PAGESIZE)
				isHaveNextPage = true;
			content.put("isHaveNextPage", isHaveNextPage);

			map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
			map.put(JsonResponser.MESSAGE, "成功");
			map.put(JsonResponser.CONTENT, content);
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}
		JsonResponser.respondWithJson(response, map);
	}

	// 向聊天室添加联系人，添加前取出现有全部联系人，并取出各部门全部人员被选
	@RequestMapping(value = "/addContacts", method = { RequestMethod.GET, RequestMethod.POST })
	public void addContacts(long roomId, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> content = new HashMap<String, Object>();

		try {
			HyqUserData userData = this.getLoginUserData(request, response);

			List<HyqUserData> roomMembers = chatRoomService.getRoomMembers(roomId);

			content.put("userData", userData);
			content.put("roomId", roomId);
			content.put("roomMembers", roomMembers);

			map.put(JsonResponser.CONTENT, content);
			map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
			map.put(JsonResponser.MESSAGE, "取得被选聊天室成员即部门人员成功");
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
	}

	// 将选定的（也有的取消了）联系人加入聊天室
	@RequestMapping(value = "/saveContacts", method = { RequestMethod.GET, RequestMethod.POST })
	public void saveContacts(Long[] contacts, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			// 构造chatRoomData
			ChatRoomData chatRoomData = new ChatRoomData();
			// 群ID
			Long roomId = ServletRequestUtils.getLongParameter(request, "roomId", 0L);
			chatRoomData.setId(roomId);
			// 保存
			int n = 0;
			if (contacts != null)
				n = contacts.length;
			Long[] users = new Long[n + 1];
			users[0] = selfData.getId();
			for (int i = 1; i <= n; i++) {
				users[i] = contacts[i - 1];
			}
			chatRoomService.saveChatRoom(chatRoomData, users);

			map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
			map.put(JsonResponser.MESSAGE, "聊天室成员添加成功");
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
	}

	// 删除聊天室，其实是退出聊天室
	@RequestMapping(value = "/delChatRoom", method = RequestMethod.POST)
	public void delChatRoom(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			Long roomId = ServletRequestUtils.getLongParameter(request, "deleteChatRoomId", 0L);

			boolean result = chatRoomService.deleteOneRoom(roomId, selfData.getId());

			if (result) {
				map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
				map.put(JsonResponser.MESSAGE, "删除成功");
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "删除失败");
			}
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
	}

	@RequestMapping(value = "/sendChatMessage", method = RequestMethod.POST)
	public void sendChatMessage(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			Long roomId = ServletRequestUtils.getLongParameter(request, "roomId", 0L);
			String content = ServletRequestUtils.getStringParameter(request, "content", "");

			Long msgId = chatMessageService.saveMessage(roomId, selfData.getId(), content);

			if (msgId > 0) {
				map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
				map.put(JsonResponser.MESSAGE, "增加成功");
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "增加失败");
			}
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
	}

	@RequestMapping(value = "/delChatMessage", method = RequestMethod.POST)
	public void delChatMessage(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			Long msgId = ServletRequestUtils.getLongParameter(request, "msgId", 0L);

			boolean result = chatMessageService.deleteOneMessage(msgId, selfData.getId());

			if (result) {
				map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
				map.put(JsonResponser.MESSAGE, "删除成功");
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "删除失败");
			}
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
	}

	// 群中选人发红包
	@RequestMapping(value = "/groupBonusSend", method = { RequestMethod.GET, RequestMethod.POST })
	public void groupBonusSend(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			String rIds = ServletRequestUtils.getStringParameter(request, "receiverIds", "");
			String[] strIds = rIds.split(",");
			long[] receiverIds = new long[strIds.length];
			for (int i = 0; i < strIds.length; i++)
				receiverIds[i] = Long.parseLong(strIds[i]);

			Long roomId = ServletRequestUtils.getLongParameter(request, "roomId", 0L);
			Double money = ServletRequestUtils.getDoubleParameter(request, "money", 0.0D);
			String remark = ServletRequestUtils.getStringParameter(request, "remark");
			String paypwd = ServletRequestUtils.getStringParameter(request, "paypwd");

			if (!userService.checkPayPwd(selfData, paypwd))
				throw new Exception("支付密码输入错误，如未设置支付密码请到设置中进行设置,以确保您的支付安全!");

			Map<Long, Long> mapRet = bonusService.groupBonusSend(roomId, selfData, receiverIds, money, remark);

			String content = "";
			if (mapRet != null) {
				String uw = "";
				for (Long key : mapRet.keySet())
					uw += key + "-" + mapRet.get(key) + ";";
				if (!"".equals(uw))
					uw = uw.substring(0, uw.length() - 1);
				content = "BONUS:" + uw + ":" + remark;
			}

			map.put("content", content);
			map.put("roomId", roomId);
			map.put("mapRet", mapRet);
			map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
			map.put(JsonResponser.MESSAGE, "红包发送成功");
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
	}

	// 拆红包
	@RequestMapping(value = "/batchBonusOpen", method = { RequestMethod.GET, RequestMethod.POST })
	public void batchBonusOpen(String bonuss, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			Map<String, Object> content = new HashMap<String, Object>();
			HyqUserData sender = null;

			List<String> bonusContent = new ArrayList<String>();
			String[] sss = bonuss.split(";");
			for (String s : sss) {
				String[] ss = s.split("-");

				Long uid = Long.parseLong(ss[0]);
				Long wid = Long.parseLong(ss[1]);

				HyqUserData u = userService.getById(uid);
				HyqWalletData w = walletBillService.getBillData(wid);
				if (sender == null) {
					sender = w.getBuyerData();
					content.put("sender", sender);
				}

				if (selfData.getId().equals(uid)) {
					try {
						bonusService.singleBonusOpen(selfData, w);
						bonusContent.add(u.getTrueName() + "   " + w.getTotalFee() + "元，已拆。");
					} catch (Exception e) {
						bonusContent.add(u.getTrueName() + "   " + e.getMessage());
					}
				} else {
					String state = "";
					if (w.getPaymentStatus() == PayStatusCode.WAIT_CONFIRM)
						state = "未拆";
					else if (w.getPaymentStatus() == PayStatusCode.TRADE_FINISHED)
						state = "已拆";
					else if (w.getPaymentStatus() == PayStatusCode.TRADE_CLOSED)
						state = "被收回";

					bonusContent.add(u.getTrueName() + "   " + w.getTotalFee() + "元，" + state + "。");
				}
			}
			map.put(JsonResponser.CONTENT, content);
			map.put("bonusContent", bonusContent);

			map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
			map.put(JsonResponser.MESSAGE, "接收红包成功！");
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
	}

	// 回收红包
	@RequestMapping(value = "/batchBonusRefund", method = { RequestMethod.GET, RequestMethod.POST })
	public void batchBonusRefund(String bonuss, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			Map<String, Object> content = new HashMap<String, Object>();
			HyqUserData sender = null;

			List<String> bonusContent = new ArrayList<String>();
			String[] sss = bonuss.split(";");
			for (String s : sss) {
				String[] ss = s.split("-");

				Long uid = Long.parseLong(ss[0]);
				Long wid = Long.parseLong(ss[1]);

				HyqUserData u = userService.getById(uid);
				HyqWalletData w = walletBillService.getBillData(wid);
				if (sender == null) {
					sender = w.getBuyerData();
					content.put("sender", sender);
				}

				if (w.getPaymentStatus() == PayStatusCode.WAIT_CONFIRM) {
					try {
						bonusService.singleBonusRefund(selfData, w);
						bonusContent.add(u.getTrueName() + "   " + w.getTotalFee() + "元，被收回。");
					} catch (Exception e) {
						bonusContent.add(u.getTrueName() + "   " + e.getMessage());
					}
				} else {
					String state = "";
					if (w.getPaymentStatus() == PayStatusCode.WAIT_CONFIRM)
						state = "未拆";
					else if (w.getPaymentStatus() == PayStatusCode.TRADE_FINISHED)
						state = "已拆";
					else if (w.getPaymentStatus() == PayStatusCode.TRADE_CLOSED)
						state = "被收回";

					bonusContent.add(u.getTrueName() + "   " + w.getTotalFee() + "元，" + state + "。");
				}
			}
			map.put(JsonResponser.CONTENT, content);
			map.put("bonusContent", bonusContent);

			map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
			map.put(JsonResponser.MESSAGE, "回收红包成功！");
		} catch (Exception e) {
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithJson(response, map);
	}

	// 图片上传
	@RequestMapping(value = "/imageUpload", method = { RequestMethod.POST })
	public void imageUpload(MultipartFile picFile, Long roomId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			String add = chatMessageService.savePic(picFile, roomId);
			if (add != "" || add != null) {
				map.put("content", add);
				map.put("imageName", picFile.getOriginalFilename());
				map.put("imageSize", picFile.getSize());
				map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
				map.put(JsonResponser.MESSAGE, selfData.getTrueName() + "上传文件成功！");
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "上传失败：路径错误！");
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}

		JsonResponser.respondWithText(response, map);
		return;
	}

	// 文件上传
	@RequestMapping(value = "/fileUpload", method = { RequestMethod.POST })
	public void fileUpload(@RequestParam("docFile") CommonsMultipartFile docFile, Long roomId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			String add = chatMessageService.saveFile(docFile, roomId);
			if (add != "" || add != null) {
				map.put("content", add);
				map.put("fileName", docFile.getOriginalFilename());
				map.put("fileSize", docFile.getSize());
				map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
				map.put(JsonResponser.MESSAGE, selfData.getTrueName() + "上传文件成功！");
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "上传失败：路径错误！");
			}

			JsonResponser.respondWithText(response, map);
		} catch (Exception e) {
			logger.error(e.getMessage());
			if (docFile.getSize() < 0 || docFile == null) {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "上传失败：文件不能为空！");
			}
			JsonResponser.respondWithText(response, map);
		}
		return;
	}

	@RequestMapping(value = "/voiceUpload", method = { RequestMethod.POST })
	public void voiceUpload(Long roomId, MultipartFile mpf, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			String url = chatMessageService.saveVoice(mpf, roomId);

			if (url != "" || url != null) {
				map.put(JsonResponser.RETURNCODE, JsonResponser.SUCCESS);
				map.put(JsonResponser.MESSAGE, selfData.getTrueName() + "上传文件成功！");
				map.put(JsonResponser.CONTENT, url);
			} else {
				map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
				map.put(JsonResponser.MESSAGE, "失败");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			map.put(JsonResponser.RETURNCODE, JsonResponser.FAILURE);
			map.put(JsonResponser.MESSAGE, e.getMessage());
		}
		JsonResponser.respondWithJson(response, map);
	}

	@RequestMapping(value = "/imageDownload", method = { RequestMethod.GET })
	public void imageDownload(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			String urlPath = ServletRequestUtils.getStringParameter(request, "url", "");
			if ("".equals(urlPath)) {
				return;
			}

			Long roomId = Long.parseLong(urlPath.substring(5, urlPath.indexOf("uploadImage-")).replaceAll("/", ""));
			if (!chatRoomService.isRoomMember(roomId, selfData.getId())) {
				return;
			}

			File file = new File(Constants.SHARE_DIR + urlPath);
			if (!file.exists()) {
				return;
			}

			response.setHeader("Content_Length", String.valueOf(file.length()));
			response.setContentType("image/jpeg");

			ServletOutputStream sos = response.getOutputStream();
			FileInputStream fis = new FileInputStream(file);

			IOUtils.copy(fis, sos);

			fis.close();
			sos.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@RequestMapping(value = "/fileDownload", method = { RequestMethod.GET })
	public void fileDownload(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			String urlPath = ServletRequestUtils.getStringParameter(request, "url", "");
			if ("".equals(urlPath)) {
				return;
			}

			Long roomId = Long.parseLong(urlPath.substring(5, urlPath.indexOf("uploadFile-")).replaceAll("/", ""));
			if (!chatRoomService.isRoomMember(roomId, selfData.getId())) {
				return;
			}

			File file = new File(Constants.SHARE_DIR + urlPath);
			if (!file.exists()) {
				return;
			}

			// 设置下载文件的类型为任意类型
			response.setContentType("application/x-msdownload");
			// 添加下载文件的头信息。此信息在下载时会在下载面板上显示，比如：
			String fileName = file.getName();
			String downLoadName = new String(fileName.getBytes("GBK"), "iso8859-1");
			response.setHeader("Content-Disposition", "attachment;filename=\"" + downLoadName + "\"");
			response.setHeader("Content_Length", String.valueOf(file.length()));

			ServletOutputStream sos = response.getOutputStream();
			FileInputStream fis = new FileInputStream(file);

			IOUtils.copy(fis, sos);

			fis.close();
			sos.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@RequestMapping(value = "/voiceDownload", method = { RequestMethod.GET })
	public void voiceDownload(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);

			String urlPath = ServletRequestUtils.getStringParameter(request, "url", "");
			if ("".equals(urlPath)) {
				return;
			}

			Long roomId = Long.parseLong(urlPath.substring(5, urlPath.indexOf("voice-")).replaceAll("/", ""));
			if (!chatRoomService.isRoomMember(roomId, selfData.getId())) {
				return;
			}

			File file = new File(Constants.SHARE_DIR + urlPath);
			if (!file.exists()) {
				return;
			}

			// 设置下载文件的类型为任意类型
			response.setContentType("application/x-msdownload");
			// 添加下载文件的头信息。此信息在下载时会在下载面板上显示，比如：
			String fileName = file.getName();
			String downLoadName = new String(fileName.getBytes("GBK"), "iso8859-1");
			response.setHeader("Content-Disposition", "attachment;filename=\"" + downLoadName + "\"");
			response.setHeader("Content_Length", String.valueOf(file.length()));

			ServletOutputStream sos = response.getOutputStream();
			FileInputStream fis = new FileInputStream(file);

			IOUtils.copy(fis, sos);

			fis.close();
			sos.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/voiceDownload2", method = { RequestMethod.GET })
	public void voiceDownload2(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);
			
			String urlPath = ServletRequestUtils.getStringParameter(request, "url", "");
			if ("".equals(urlPath)) {
				return;
			}
			
//			Long roomId = Long.parseLong(urlPath.substring(5, urlPath.indexOf("voice-")).replaceAll("/", ""));
//			if (!chatRoomService.isRoomMember(roomId, selfData.getId())) {
//				return;
//			}
			
			File file = new File(Constants.SHARE_DIR + urlPath);
			if (!file.exists()) {
				return;
			}
			
			// 设置下载文件的类型为任意类型
			response.setContentType("application/x-msdownload");
			// 添加下载文件的头信息。此信息在下载时会在下载面板上显示，比如：
			String fileName = file.getName();
			String downLoadName = new String(fileName.getBytes("GBK"), "iso8859-1");
			response.setHeader("Content-Disposition", "attachment;filename=\"" + downLoadName + "\"");
			response.setHeader("Content_Length", String.valueOf(file.length()));
			
			ServletOutputStream sos = response.getOutputStream();
			FileInputStream fis = new FileInputStream(file);
			
			IOUtils.copy(fis, sos);
			
			fis.close();
			sos.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/logoDownload", method = { RequestMethod.GET })
	public void logoDownload(Long userId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			HyqUserData selfData = this.getLoginUserData(request, response);
			
			HyqUserData ud = userService.getById(userId);
			if(ud == null)
				throw new Exception("找不到该用户");
			File file = new File(Constants.SHARE_DIR + ud.getUserLogo());
			if (!file.exists()) {
				return;
			}
			
			response.setHeader("Content_Length", String.valueOf(file.length()));
			response.setContentType("image/jpeg");
			ServletOutputStream sos = response.getOutputStream();
			FileInputStream fis = new FileInputStream(file);
			IOUtils.copy(fis, sos);
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		}
	}

}
