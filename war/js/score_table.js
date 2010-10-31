/**
 * Logic for the table of scores on partychapp channel pages.
 */

goog.provide('partychapp.ScoreTable');

/**
 * @param {Date} date
 */
function formatDate(date) {
 function pad(n) {return n < 10 ? '0' + n : n}
 return pad(date.getMonth() + 1) + '/' +
        pad(date.getDate())+ '/' +
        date.getFullYear();
}

/**
 * @param {string} targetName
 * @param {Node} targetCellNode
 * @param {Object} data
 */
function addTargetDetails(targetName, targetCellNode, data) {
  var reasonsNode = goog.dom.$dom('ul', 'reasons');

  for (var i = 0, reason; reason = data['reasons'][i]; i++) {
    var reasonNode = goog.dom.$dom('li');

    var actionNode = goog.dom.$dom('span', 'action');
    goog.dom.classes.enable(actionNode, 'plusplus', reason.action == '++');
    goog.dom.classes.enable(actionNode, 'minusminus', reason.action != '++');
    actionNode.innerHTML = reason['action'];
    reasonNode.appendChild(actionNode);

    var senderConnectorNode = goog.dom.$dom('span', {}, '\'ed by ');
    reasonNode.appendChild(senderConnectorNode);

    var senderNode = goog.dom.$dom('span', 'sender', reason['sender']);
    reasonNode.appendChild(senderNode);

    // Only show the short version of the reason (if any), so strip out
    // everything before the action...
    var targetLocation = reason.reason.toLowerCase().indexOf(
        targetName.toLowerCase() + reason['action']);
    if (targetLocation != -1) {
      var reasonDetails = reason.reason.substring(
          targetLocation + targetName.length + 2);

      if (!goog.string.isEmptySafe(reasonDetails)) {
        // ...but still have the full reason line as a tooltip.
        var reasonDetailsNode = goog.dom.$dom('span', {
          'title': reason['reason']
        }, reasonDetails);
        reasonNode.appendChild(reasonDetailsNode);
      }
    }

    var dateNode = goog.dom.$dom(
        'span',
        'date',
        ' on ' + formatDate(new Date(reason['timestampMsec'])));
    reasonNode.appendChild(dateNode);

    reasonsNode.appendChild(reasonNode);
  }

  var graphNode = goog.dom.$dom('img', {
    'src': data['graph']
  });

  var detailsNode =
      goog.dom.$dom('div', 'target-details', graphNode, reasonsNode);
  targetCellNode.appendChild(detailsNode);
}
goog.exportSymbol('addTargetDetails', addTargetDetails);

/**
 * @param {Node} targetNameNode
 * @param {string} channelName
 * @param {string} targetName
 */
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
goog.exportSymbol('toggleTargetDetails', toggleTargetDetails);

/**
 * @enum {number}
 */
var SortOrder = {
  BY_NAME: 1,
  BY_SCORE: 2
};

/** @const */
var UP_ARROW = '&#8679;';
/** @const */
var DOWN_ARROW = '&#8681;';

/**
 * @constructor
 * @param {string} channelName
 * @param {Array.<Object>} targetList
 */
partychapp.ScoreTable = function(channelName, targetList) {
  /**
   * @type {string}
   */
  this.channelName = channelName;
  
  /**
   * @type {Array.<Object>}
   */
  this.targetList = targetList;
  
  this.sortByName();
}

/**
 * @type {SortOrder}
 */
partychapp.ScoreTable.prototype.sortOrder;

partychapp.ScoreTable.prototype.sortByName = function() {
  if (this.sortOrder == SortOrder.BY_NAME) {
    this.targetList.reverse();
    this.toggleArrow();
  } else {
    this.sortOrder = SortOrder.BY_NAME;
    this.arrow = UP_ARROW;
    this.targetList.sort(function(a, b) { return a['name'].localeCompare(b['name']); });
  }

  this.draw();
}

partychapp.ScoreTable.prototype.toggleArrow = function() {
  if (this.arrow == DOWN_ARROW) {
    this.arrow = UP_ARROW;
  } else {
    this.arrow = DOWN_ARROW;
  }
}

partychapp.ScoreTable.prototype.sortByScore = function() {
  if (this.sortOrder == SortOrder.BY_SCORE) {
    this.targetList.reverse();
    this.toggleArrow();
  } else {
    this.sortOrder = SortOrder.BY_SCORE;
    this.arrow = DOWN_ARROW;
    this.targetList.sort(function(a, b) { return b['score'] - a['score']; });
  }

  this.draw();
}

partychapp.ScoreTable.prototype.draw = function() {
  soy.renderElement(
      goog.dom.$('score-table'),
      partychapp.templates.scoreTable,
      { channelName: this.channelName,
        targets: this.targetList });

  var nameHeader = goog.dom.$('target-name-header');
  var scoreHeader = goog.dom.$('target-score-header');

  nameHeader.onclick = goog.bind(this.sortByName, this);
  scoreHeader.onclick = goog.bind(this.sortByScore, this);

  if (this.sortOrder == SortOrder.BY_NAME) {
    nameHeader.innerHTML = this.arrow + nameHeader.innerHTML;
  } else {
    scoreHeader.innerHTML = this.arrow + scoreHeader.innerHTML;
  }
}

goog.exportSymbol('partychapp.ScoreTable', partychapp.ScoreTable);