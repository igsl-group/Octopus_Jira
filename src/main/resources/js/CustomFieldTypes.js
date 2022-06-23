var igsl_effortTable = {};
var igsl_staticImage = {};
var igsl_observer = null;
function igsl_customFieldInit() {
	// Get list of custom fields, find those with type "IGSL Effort Table" (i.e. i18n key)
	AJS.$.ajax({
		url: AJS.contextPath() + '/rest/api/latest/customFields',
		contentType:'application/json',
		method: 'GET',
		data: null,
		dataType: 'json'
	}).done(function(data) {
		if (data && data.values && data.values.length) {
			for (var i = 0; i < data.values.length; i++) {
				if (data.values[i].type == "IGSL Effort Table") {
					igsl_effortTable[data.values[i].id] = data.values[i].name;
				}
				if (data.values[i].type == "IGSL Static Image") {
					igsl_staticImage[data.values[i].id] = data.values[i].name;
				}
			}
		}
		// Invoke once
		igsl_customFieldCheckPage();
		// Register DOM observer
		var observerConfig = { 
			attributes: false, 
			childList: true, 
			subtree: true 
		};
		igsl_observer = new MutationObserver(igsl_customFieldCheckPage);
		igsl_observer.observe(document, observerConfig);
	});
}
AJS.toInit(igsl_customFieldInit);

function igsl_customFieldCheckPage(mutationList, observer) {
	// Page in Customer Portal
	if (document.location.pathname && document.location.pathname.startsWith(AJS.contextPath() + '/servicedesk/customer/portal/')) {
		// Effort table
		for (var item in igsl_effortTable) {
			// View 
			AJS.$('section.cv-request-activity ul.vp-activity-list div dl dt:contains("' + igsl_effortTable[item] + '")').siblings('dd').each(function() {
				igsl_effortTableView(AJS.$(this), null);
			});
			// Edit 
			AJS.$('div.cv-request-create-container form div.field-group div.field-container input[id="' + item + '"]').each(function() {
				AJS.$(this).parent('div').parent('div').hide();
			});
		}
		// Static Image
		for (var item in igsl_staticImage) {
			// View 
			AJS.$('section.cv-request-activity ul.vp-activity-list div dl dt:contains("' + igsl_staticImage[item] + '")').siblings('dd').each(function() {
				var issueKey = AJS.$('a.js-request-reference')[0].href;
				issueKey = issueKey.substring(issueKey.lastIndexOf('/') + 1);
				igsl_staticImageView(AJS.$(this), issueKey, item, null);
			});
			// Edit 
			AJS.$('div.cv-request-create-container form div.field-group div.field-container input[id="' + item + '"]').each(function() {
				igsl_staticImageView(AJS.$(this), null, null, null);
			});
		}
	}
	// Dialog in Customer Portal
	var iFrame = AJS.$('iframe[id="portal-frame"][src*="' + AJS.contextPath() + '/servicedesk/customer/portal/"]')[0];
	if (iFrame) {
		// Effort table
		for (var item in igsl_effortTable) {
			// View 
			AJS.$(iFrame).contents().find('section.cv-request-activity ul.vp-activity-list div dl dt:contains("' + igsl_effortTable[item] + '")').siblings('dd').each(function() {
				igsl_effortTableView(AJS.$(this), null);
			});
			// Edit 
			AJS.$(iFrame).contents().find('div.cv-request-create-container form div.field-group div.field-container input[id="' + item + '"]').each(function() {
				AJS.$(this).parent('div').parent('div').hide();
			});
		}
		// Static Image
		for (var item in igsl_staticImage) {
			// View 
			AJS.$(iFrame).contents().find('section.cv-request-activity ul.vp-activity-list div dl dt:contains("' + igsl_staticImage[item] + '")').siblings('dd').each(function() {
				var issueKey = AJS.$('a.js-request-reference')[0].href;
				issueKey = issueKey.substring(issueKey.lastIndexOf('/') + 1);
				igsl_staticImageView(AJS.$(this), issueKey, item, null);
			});
			// Edit 
			AJS.$(iFrame).contents().find('div.cv-request-create-container form div.field-group div.field-container input[id="' + item + '"]').each(function() {
				igsl_staticImageView(AJS.$(this), null, null, null);
			});
		}
	}
}

function igsl_staticImageDefaultValue(issueKey, customFieldId, action) {
	var data = {
		issueKey: issueKey,
		customFieldId: customFieldId
	};
	AJS.$.ajax({
		url: AJS.contextPath() + '/rest/igsl/latest/getCustomFieldDefaultValue',
		type: 'POST',
		contentType: 'application/json',
		data: JSON.stringify(data),
		dataType: 'json'
	}).done(function(data) {
		action(data);
	});
}

// Create image tag replacig provided JQuery object.
function igsl_staticImageView(jq, issueKey, customFieldId, data) {
	jq.each(function() {
		var ctrl = AJS.$(this);
		if (ctrl.is(':hidden')) {
			// Already processed
			return;
		}
		ctrl.hide();
		if (!data) {
			if (issueKey != null && customFieldId != null) {
				igsl_staticImageDefaultValue(issueKey, customFieldId, function(defaultValue) {
					var img = AJS.$('<img/>');
					img.prop('src', defaultValue.result);
					ctrl.after(img);
				});
			} else {
				var img = AJS.$('<img/>');
				img.prop('src', ctrl.val());
				ctrl.prop('value', '.');	// Set dummy value
				ctrl.after(img);
			}
		} else {
			var img = AJS.$('<img/>');
			img.prop('src', data);
			ctrl.after(img);
		}
	});
}

// Create read-only Effort table from text of provided JQuery object.
function igsl_effortTableView(jq, data) {
	jq.each(function() {
		var ctrl = AJS.$(this);
		if (ctrl.is(':hidden')) {
			// Already processed
			return;
		}
		var effortData = null;
		if (!data) {
			effortData = JSON.parse(ctrl.text());
		} else {
			effortData = JSON.parse(data);
		}
		var table = AJS.$('<table/>');
		table.addClass('view-table');
		// Table header
		var colTask = AJS.I18n.getText('EffortTable.task.label');
		var colHeadCountDay = AJS.I18n.getText('EffortTable.headCountDay.label');
		table.append('<thead><tr><th>' + colTask + '</th><th>' + colHeadCountDay + '</th></tr></thead>');
		var tbody = AJS.$('<tbody/>');
		if (effortData.rows && effortData.rows.length) {
			for (var i = 0; i < effortData.rows.length; i++) {
				tbody.append('<tr><td>' + effortData.rows[i].task + '</td><td>' + effortData.rows[i].headCountDay + '</td></tr>');		
			}
		}
		var tfoot = AJS.$('<tfoot/>');
		var colExpenses = AJS.I18n.getText('EffortTable.expenses.label');
		var colTotalHeadCountDay = AJS.I18n.getText('EffortTable.totalHeadCountDay.label');
		tfoot.append('<tr><th>' + colExpenses + '</th><th>' + colTotalHeadCountDay + '</th></tr>');
		var expenses = (effortData.expenses)? effortData.expenses : '';
		var totalHeadCountDay = (effortData.totalHeadCountDay)? effortData.totalHeadCountDay : '';
		tfoot.append('<tr><td>' + expenses + '</td><td>' + totalHeadCountDay + '</td></tr>');
		table.append(tbody);
		table.append(tfoot);
		ctrl.hide();
		ctrl.after(table);
	});
}