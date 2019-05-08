package com.hangyi.eyunda.data.hyquan;

import java.util.Map;

import com.hangyi.eyunda.data.BaseData;
import com.hangyi.eyunda.domain.enumeric.YesNoCode;

public class HyqWalletSettleData extends BaseData {

	private static final long serialVersionUID = -1L;

	private String createTime = ""; // 建立时间
	private Long userId = 0L; // 用户ID
	private Long walletId = 0L; // 钱包交易ID
	private String subject = ""; // 标题
	private Double money = 0.00D; // 交易金额，可正可负
	private Double usableMoney = 0.00D; // 可用余额
	private Double fetchableMoney = 0.00D; // 可取余额，大于等于0，负数表示是今天交易记录

	private HyqUserData userData = null;
	private Double jzbTotalBalance = 0.00D; // 见证宝可用余额
	private Double jzbTotalTranOutAmount = 0.00D; // 见证宝可取余额
	private YesNoCode checkOpt = YesNoCode.no; // 是否可查询

	private String payDate = "";
	private String payTime = "";

	public HyqWalletSettleData() {
		super();
	}

	@SuppressWarnings("unchecked")
	public HyqWalletSettleData(Map<String, Object> params) {
		super();
		if (params != null && !params.isEmpty()) {
			this.userId = ((Double) params.get("userId")).longValue();
			this.walletId = ((Double) params.get("walletId")).longValue();

			this.createTime = (String) params.get("createTime");
			this.payDate = (String) params.get("payDate");
			this.payTime = (String) params.get("payTime");
			this.subject = (String) params.get("subject");

			this.money = (Double) params.get("money");
			this.usableMoney = (Double) params.get("usableMoney");
			this.fetchableMoney = (Double) params.get("fetchableMoney");
			this.jzbTotalBalance = (Double) params.get("jzbTotalBalance");
			this.jzbTotalTranOutAmount = (Double) params.get("jzbTotalTranOutAmount");

			this.userData = new HyqUserData((Map<String, Object>) params.get("userData"));

			String checkOpt = (String) params.get("checkOpt");
			if ((checkOpt != null) && (!checkOpt.equals(""))) {
				this.checkOpt = YesNoCode.valueOf(checkOpt);
			}
		}
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getPayDate() {
		return payDate;
	}

	public void setPayDate(String payDate) {
		this.payDate = payDate;
	}

	public String getPayTime() {
		return payTime;
	}

	public void setPayTime(String payTime) {
		this.payTime = payTime;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getWalletId() {
		return walletId;
	}

	public void setWalletId(Long walletId) {
		this.walletId = walletId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	public Double getUsableMoney() {
		return usableMoney;
	}

	public void setUsableMoney(Double usableMoney) {
		this.usableMoney = usableMoney;
	}

	public Double getFetchableMoney() {
		return fetchableMoney;
	}

	public void setFetchableMoney(Double fetchableMoney) {
		this.fetchableMoney = fetchableMoney;
	}

	public HyqUserData getUserData() {
		return userData;
	}

	public void setUserData(HyqUserData userData) {
		this.userData = userData;
	}

	public Double getJzbTotalBalance() {
		return jzbTotalBalance;
	}

	public void setJzbTotalBalance(Double jzbTotalBalance) {
		this.jzbTotalBalance = jzbTotalBalance;
	}

	public Double getJzbTotalTranOutAmount() {
		return jzbTotalTranOutAmount;
	}

	public void setJzbTotalTranOutAmount(Double jzbTotalTranOutAmount) {
		this.jzbTotalTranOutAmount = jzbTotalTranOutAmount;
	}

	public YesNoCode getCheckOpt() {
		return checkOpt;
	}

	public void setCheckOpt(YesNoCode checkOpt) {
		this.checkOpt = checkOpt;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}