// Adding approval panel to customer portal

console.log('CustomApproval_Portal.js loaded');

var customApproval_observer = null;
var customApproval_RetryDelay = 1000;
var customApproval_iFrame = null;
var customApproval_IssueKey = null;
var customApproval_Sidebar = null;

function showConfirmDialog(showDialog, approve, link, issueKey) {
	// Show confirm dialog
	if (showDialog == 'true') {
		if (approve) {
			AJS.$('#approve-message').show();
			AJS.$('#reject-message').hide();
		} else {
			AJS.$('#approve-message').hide();
			AJS.$('#reject-message').show();
		}
		AJS.$('#confirm-dialog-link').val(link);
		AJS.$('#confirm-dialog-issueKey').val(issueKey);
		AJS.dialog2("#confirm-dialog").show();
	} else {
		customApproval_Submit(link, issueKey);
	}
}

function customApproval_Submit(link, issueKey) {
	AJS.$.ajax({
		url: AJS.contextPath() + link,
		contentType:'application/json',
		method: 'POST',
		data: issueKey,
		dataType: 'json'
	}).done(function(data) {
		// Reload page
		location.reload();
	});
}

function customApproval_addPanelData() {
	// Add custom approval panel
	if (customApproval_Sidebar.length == 1) {
		if (customApproval_Sidebar.find('div#approvalPanel').length == 0) {
			AJS.$.ajax({
				url: AJS.contextPath() + '/rest/igsl/latest/getApprovalData',
				contentType:'application/json',
				method: 'POST',
				data: customApproval_IssueKey,
				dataType: 'json'
			}).done(function(data) {
				var buttonPanel = AJS.$('<div></div>');
				var historyPanel = AJS.$('<div></div>');
				if (data.approveLink) {
					// Add approve button
					buttonPanel.append(
						'<button onclick="showConfirmDialog(\'' + 
						data.settings.confirmDecision + 
						'\', true, \'' + 
						data.approveLink + 
						'\', \'' + 
						customApproval_IssueKey + 
						'\')">Approve</button>');
				}
				if (data.rejectLink) {
					// Add reject button
					buttonPanel.append(
						'<button onclick="showConfirmDialog(\'' + 
						data.settings.confirmDecision + 
						'\', false, \'' + 
						data.rejectLink + 
						'\', \'' + 
						customApproval_IssueKey + 
						'\')">Reject</button>');
				}
				if (data.data) {
					var panelData = data.data;
					// Add approval history table
					for (var i in panelData) {
						var title = panelData[i].approvalName;
						if (panelData[i].completed) {
							if (panelData[i].approved) {
								title += ' - Approved';
							} else {
								title += ' - Rejected';	
							}
						} else {
							title += ' - Pending';
						}
						var count = 
							'(Approve ' + 
							panelData[i].approveCount + '/' + panelData[i].approveCountTarget + 
							' Reject ' + 
							panelData[i].rejectCount + '/' + panelData[i].rejectCountTarget + 
							')';
						historyPanel.append('<div class="CustomApprovalCustomerPortalTite">' + title + '</div>');
						historyPanel.append('<div class="CustomApprovalCustomerPortalCount">' + count + '</div>')
						var table = AJS.$(
							'<table class="CustomApprovalCustomerPortal">' +
								'<thead>' + 
									'<tr>' + 
										'<th width="45%">Approver</th>' + 
										'<th width="10%">Decision</th>' + 
										'<th width="45%">Date</th>' + 
									'</tr>' + 
							 	'</thead>' + 
							 	'<tbody></tbody>' + 
							 '</table>');
						var history = panelData[i].sortedHistory;
						if (history.length == 0) {
							AJS.$(table).find('tbody').append(
								'<tr><td colspan="100%">No approvals yet</td></tr>'
							);
						} else {
							for (var i in history) {
								var name;
								if (history[i].delegatedDisplayName != null) {
									name = history[i].delegatedDisplayName + ' on behalf of ' + 
										history[i].approverDisplayName;
								} else {
									name = history[i].approverDisplayName;
								}
								var cls = (history[i].valid)? '' : 'CustomApprovalStrikeOut';
								AJS.$(table).find('tbody').append(
									'<tr>' + 
										'<td class="' + cls + '">' + name + '</td>' + 
										'<td class="' + cls + '">' + history[i].decision + '</td>' + 
										'<td class="' + cls + '">' + history[i].approvedDateString + '</td>' + 
									'</tr>'
								);
							}
						}
						historyPanel.append(table);
					}
				}
				customApproval_Sidebar.append('<ul class="cv-request-actions"><li><div id="approvalPanel"></div></li></ul>');
				AJS.$('div#approvalPanel').append('<h5>Approval</h5>');
				AJS.$('div#approvalPanel').append(buttonPanel);
				AJS.$('div#approvalPanel').append('<br/>');
				AJS.$('div#approvalPanel').append(historyPanel);
				
				// Confirm dialog
				var confirmDialog = AJS.$(
					'<section id="confirm-dialog" class="aui-dialog2 aui-dialog2-small aui-layer" role="dialog" aria-hidden="true">' + 
					'	<header class="aui-dialog2-header">' + 
					'		<h2 class="aui-dialog2-header-main">' + data.settings.confirmTitle + '</h2>' + 
					'	</header>' + 
					'	<div class="aui-dialog2-content">' + 
					'		<input type="hidden" id="confirm-dialog-link" value=""/>' + 
					'		<input type="hidden" id="confirm-dialog-issueKey" value=""/>' + 
					'		<p id="approve-message">' + data.settings.approveMessage + '</p>' + 
					'		<p id="reject-message">' + data.settings.rejectMessage + '</p>' + 
					'	</div>' + 
					'	<footer class="aui-dialog2-footer">' + 
					'		<div class="aui-dialog2-footer-actions">' + 
					'			<button id="confirm-dialog-ok" class="aui-button aui-button-primary">' + data.settings.confirmOK + '</button>' + 
					'			<button id="confirm-dialog-cancel" class="aui-button aui-button-link">' + data.settings.confirmCancel + '</button>' + 
					'		</div>' + 
					'	</footer>' + 
					'</section>'
				);
				AJS.$('div#approvalPanel').append(confirmDialog);
				AJS.$("#confirm-dialog-ok").click(function(e) {
				    e.preventDefault();
				    var link = AJS.$('#confirm-dialog-link').val();
				    var issueKey = AJS.$('#confirm-dialog-issueKey').val();
				    AJS.dialog2("#confirm-dialog").hide();
				    customApproval_Submit(link, issueKey);
				});
				AJS.$("#confirm-dialog-cancel").click(function (e) {
				    e.preventDefault();
				    AJS.dialog2("#confirm-dialog").hide();
				});

			});
		}
	}
}

function customApproval_init() {
	if (document.location.pathname && 
		document.location.pathname.startsWith(AJS.contextPath() + '/servicedesk/customer/portal/')) {
		customApproval_iFrame = null;
	}
	var iFrame = AJS.$(
		'iframe[id="portal-frame"][src*="' + AJS.contextPath() + '/servicedesk/customer/portal/"]')[0];
	if (iFrame) {
		customApproval_iFrame = iFrame;
	}
	customApproval_IssueKey = location.href.substring(location.href.lastIndexOf('/') + 1);
	var sidebarKey = 'aside.aui-page-panel-sidebar div.cv-request-options';
	var sidebar = null;
	if (customApproval_iFrame) {
		sidebar = AJS.$(customApproval_iFrame).contents().find(sidebarKey);
	} else {
		sidebar = AJS.$(sidebarKey);
	}
	if (sidebar && sidebar.length == 1) {
		customApproval_Sidebar = sidebar;
	} 
	if (customApproval_Sidebar != null && customApproval_IssueKey != null) {
		customApproval_addPanelData();
	}
	setTimeout(customApproval_init, customApproval_RetryDelay);
}

function checkAJS() {
	if (AJS && AJS.toInit) {
		console.log('AJS ready');
		AJS.toInit(customApproval_init);
	} else {
		console.log('Checking AJS');
		setTimeout(checkAJS, 100);		
	}
}

// Note: With PluginSDK update, AJS.toInit fails to work immediately. 
// So wait until it becomes available
console.log('Checking AJS');
setTimeout(checkAJS, 100);

