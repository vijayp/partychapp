// This file was automatically generated from partychapp.soy.
// Please don't edit this file by hand.

if (typeof partychapp == 'undefined') { var partychapp = {}; }
if (typeof partychapp.templates == 'undefined') { partychapp.templates = {}; }


/**
 * @param {Object.<string, *>=} opt_data
 * @return {string}
 * @notypecheck
 */
partychapp.templates.scoreTable = function(opt_data) {
  var output = '<table class="channel-table"><tr><th class="target-cell" id="target-name-header" style="cursor: pointer; cursor: hand">Target</th><th class="score-cell" id="target-score-header" style="cursor: pointer; cursor: hand">Score</th></tr>';
  if (opt_data.targets.length == 0) {
    output += '<tr><td>No scores yet! Start ++\'ing and --\'ing stuff!</td></tr>';
  } else {
    var targetList8 = opt_data.targets;
    var targetListLen8 = targetList8.length;
    for (var targetIndex8 = 0; targetIndex8 < targetListLen8; targetIndex8++) {
      var targetData8 = targetList8[targetIndex8];
      output += partychapp.templates.singleTarget({channelName: opt_data.channelName, target: targetData8});
    }
  }
  output += '</table>';
  return output;
};


/**
 * @param {Object.<string, *>=} opt_data
 * @return {string}
 * @notypecheck
 */
partychapp.templates.singleTarget = function(opt_data) {
  return '<tr><td class="target-cell"><div class="target-name" onclick="toggleTargetDetails(this, \'' + soy.$$escapeHtml(opt_data.channelName) + '\', \'' + soy.$$escapeHtml(opt_data.target['name']) + '\')">' + soy.$$escapeHtml(opt_data.target['name']) + '</div></td><td class="score-cell">' + soy.$$escapeHtml(opt_data.target['score']) + '</td></tr>';
};
