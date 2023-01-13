package com.igsl.customapproval.panel;

import java.util.Comparator;

public class ApprovalPanelHistoryComparator implements Comparator<ApprovalPanelHistory> {
		
	@Override
	public int compare(ApprovalPanelHistory o1, ApprovalPanelHistory o2) {
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 != null && o2 == null) {
			return 1;
		} else if (o1 == null && o2 != null) {
			return -1;
		}
		// Compare date
		if (o1.getApprovedDate() != null && o2.getApprovedDate() == null) {
			return -1;
		} else if (o1.getApprovedDate() == null && o2.getApprovedDate() != null) {
			return 1;
		} else if (o1.getApprovedDate() != null && o2.getApprovedDate() != null) {
			int r1 = o1.getApprovedDate().compareTo(o2.getApprovedDate());
			if (r1 == 0) {
				int r2 = o1.getApprover().compareTo(o2.getApprover());
				if (r2 == 0) {
					return o1.getDelegated().compareTo(o2.getDelegated());
				} else {
					return r2;
				}
			} else {
				return r1;
			}
		} else {
			// Both date null, compare approver/delegated display name
			int r1 = o1.getApprover().compareTo(o2.getApprover());
			if (r1 == 0) {
				// Same approver, compare delegate
				if (o1.getDelegated() == null && o2.getDelegated() == null) {
					return r1;
				} else if (o1.getDelegated() != null && o2.getDelegated() == null) {
					return o1.getDelegated().compareTo(o2.getApprover());
				} else if (o1.getDelegated() == null && o2.getDelegated() != null) {
					return o1.getApprover().compareTo(o2.getDelegated());
				} else {
					return o1.getDelegated().compareTo(o2.getDelegated());
				}
			} else {
				return r1;
			}
		}
	}

}
