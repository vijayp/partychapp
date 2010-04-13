goog.require('goog.dom');
goog.require('goog.dom.classes');
goog.require('goog.net.XhrIo');
goog.require('goog.string');

function show(elt) {
  document.getElementById('actionOptions').style.display = 'none';
  document.getElementById(elt).style.display = '';
}

function formatDate(date) {
 function pad(n) {return n < 10 ? '0' + n : n}
 return pad(date.getMonth() + 1) + '/' +
        pad(date.getDate())+ '/' +
        date.getFullYear();
}

function addTargetDetails(targetName, targetCellNode, data) {
	var reasonsNode = goog.dom.$dom('ul', 'reasons');

  for (var i = 0, reason; reason = data.reasons[i]; i++) {
    var reasonNode = goog.dom.$dom('li');

    var actionNode = goog.dom.$dom('span', 'action');
    goog.dom.classes.enable(actionNode, 'plusplus', reason.action == '++');
    goog.dom.classes.enable(actionNode, 'minusminus', reason.action != '++');
    actionNode.innerHTML = reason.action;
    reasonNode.appendChild(actionNode);

    var senderConnectorNode = goog.dom.$dom('span', {}, '\'ed by ');
    reasonNode.appendChild(senderConnectorNode);

    var senderNode = goog.dom.$dom('span', 'sender', reason.sender);
    reasonNode.appendChild(senderNode);

    // Only show the short version of the reason (if any), so strip out
    // everything before the action...
    var targetLocation = reason.reason.toLowerCase().indexOf(
        targetName.toLowerCase() + reason.action);
    if (targetLocation != -1) {
      var reasonDetails = reason.reason.substring(
          targetLocation + targetName.length + 2);

      if (!goog.string.isEmptySafe(reasonDetails)) {
        // ...but still have the full reason line as a tooltip.
        var reasonDetailsNode = goog.dom.$dom('span', {
          'title': reason.reason
        }, reasonDetails);
        reasonNode.appendChild(reasonDetailsNode);
      }
    }

    var dateNode = goog.dom.$dom(
        'span',
        'date',
        ' on ' + formatDate(new Date(reason.timestampMsec)));
    reasonNode.appendChild(dateNode);

    reasonsNode.appendChild(reasonNode);
  }

  var graphNode = goog.dom.$dom('img', {
    'src': data.graph
  });

	var detailsNode =
	    goog.dom.$dom('div', 'target-details', graphNode, reasonsNode);
	targetCellNode.appendChild(detailsNode);
}

function toggleTargetDetails(targetNameNode, channelName, targetName) {
  var targetCellNode = targetNameNode.parentNode;
  var targetRowNode = targetCellNode.parentNode;

  goog.dom.classes.toggle(targetRowNode, 'target-expanded');

  // Check if we've already populated the details node
  if (goog.dom.$$('div', 'target-details', targetCellNode).length != 0) {
    return;
  }

  goog.dom.classes.add(targetRowNode, 'target-loading');

  // Otherwise fill it in (this will only happen when expanding the first time)
	var url = '/targetdetailsjson/' + channelName + '/' + targetName;
  goog.net.XhrIo.send(url, function(e) {
    goog.dom.classes.remove(targetRowNode, 'target-loading');
    var xhr = e.target;
    addTargetDetails(targetName, targetCellNode, xhr.getResponseJson());
  });
}
