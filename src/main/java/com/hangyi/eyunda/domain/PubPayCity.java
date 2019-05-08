package com.hangyi.eyunda.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 通过省级表的nodeCode字段，关联城市表的nodeCode字段，从而找出每个省下面有哪些城市。
 * 而通过城市表的oraAreaCode字段，就可以在行名行号数据里找到某个地区下的行名行号数据。
 */
@Entity 
@Table(name = "PubPayCity")
public class PubPayCity extends BaseEntity {
	private static final long serialVersionUID = -1L;

	@Column(nullable = false, length = 6)
	private String areaCode = ""; // 市县编码
	@Column(nullable = false, length = 60)
	private String areaName = ""; // 市县名称
	@Column(nullable = false, length = 1)
	private String areaType = ""; // 1市,2县
	@Column(nullable = false, length = 4)
	private String nodeCode = ""; // 省份编码
	@Column(nullable = true, length = 6)
	private String topAreaCode1 = ""; // 上级编码
	@Column(nullable = true, length = 6)
	private String topAreaCode2 = ""; // 上级编码
	@Column(nullable = true, length = 6)
	private String topAreaCode3 = ""; // 上级编码
	@Column(nullable = false, length = 4)
	private String oraAreaCode = ""; // 市县编码

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getAreaType() {
		return areaType;
	}

	public void setAreaType(String areaType) {
		this.areaType = areaType;
	}

	public String getNodeCode() {
		return nodeCode;
	}

	public void setNodeCode(String nodeCode) {
		this.nodeCode = nodeCode;
	}

	public String getTopAreaCode1() {
		return topAreaCode1;
	}

	public void setTopAreaCode1(String topAreaCode1) {
		this.topAreaCode1 = topAreaCode1;
	}

	public String getTopAreaCode2() {
		return topAreaCode2;
	}

	public void setTopAreaCode2(String topAreaCode2) {
		this.topAreaCode2 = topAreaCode2;
	}

	public String getTopAreaCode3() {
		return topAreaCode3;
	}

	public void setTopAreaCode3(String topAreaCode3) {
		this.topAreaCode3 = topAreaCode3;
	}

	public String getOraAreaCode() {
		return oraAreaCode;
	}

	public void setOraAreaCode(String oraAreaCode) {
		this.oraAreaCode = oraAreaCode;
	}

}