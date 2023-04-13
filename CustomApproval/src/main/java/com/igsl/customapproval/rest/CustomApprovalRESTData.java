package com.igsl.customapproval.rest;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.igsl.customapproval.data.ApprovalSettings;
import com.igsl.customapproval.panel.ApprovalPanelData;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomApprovalRESTData {
	private ApprovalSettings settings;
	private Collection<ApprovalPanelData> data;
	private String approveLink;
	private String rejectLink;
	public Collection<ApprovalPanelData> getData() {
		return data;
	}
	public void setData(Collection<ApprovalPanelData> data) {
		this.data = data;
	}
	public String getApproveLink() {
		return approveLink;
	}
	public void setApproveLink(String approveLink) {
		this.approveLink = approveLink;
	}
	public String getRejectLink() {
		return rejectLink;
	}
	public void setRejectLink(String rejectLink) {
		this.rejectLink = rejectLink;
	}
	public ApprovalSettings getSettings() {
		return settings;
	}
	public void setSettings(ApprovalSettings settings) {
		this.settings = settings;
	}
}
