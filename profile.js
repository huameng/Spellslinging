function doIt(name, propername) {
  var hhs = [];
  var hhl = [];
  var xhr = readTextFile("http://localhost:8000/histories/" + name + "standard.txt");
  var xhr2 = readTextFile("http://localhost:8000/histories/" + name + "legacy.txt");
  xhr.onload = function (e) {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        var lines = xhr.responseText.split('\n');
        console.log(lines.length);
        console.log(hhs);
        for (var i=0;i<lines.length;++i) {
          hhs.push(parseFloat(lines[i]));
        }
        xhr2.onload = function (e) {
          if (xhr2.readyState === 4) {
            if (xhr2.status === 200) {
              var lines = xhr2.responseText.split('\n');
              console.log(lines.length);
              console.log(hhl);
              for (var i=0;i<lines.length;++i) {
                hhl.push(parseFloat(lines[i]));
              }
              console.log(hhl);
              makeGraph(hhs,hhl,propername);
              console.log("hello, world");
            } else {
              console.error(xhr2.statusText);
            }
          }
        };
        console.log("hello, world");
      } else {
        console.error(xhr.statusText);
      }
    }
  };
}

function makeGraph(hhs,hhl,propername) {
  var xScale = d3.scale.linear()
   .domain([0, 20])
   .range([50, 1050]);
  var yScale = d3.scale.linear()
   .domain([2100, 1400])
   .range([20, 820]);
  var xAxis = d3.svg.axis()
    .scale(xScale)
    .orient("bottom");
  var yAxis = d3.svg.axis()
    .scale(yScale)
    .orient("left");
  var vis = d3.select("#graph")
              .append("svg");
  var w = 1100, h = 850;
    
  vis
    .attr("width", w)
    .attr("height", h);
  vis.append("text")
    .attr("x", 100)
    .attr("y", 20)
    .style("font-size", "18px")
    .text(propername + " Rating Graph")
  var nodes = [];
  var nodesl = [];
  for(var i=1;i<=hhs.length;++i) {
    nodes.push({x:i, y:hhs[i-1]});
  }
  for(var i=1;i<=hhl.length;++i) {
    nodesl.push({x:i, y:hhl[i-1]});
  }
  var links = [];
  for(var i=1;i<nodes.length;++i) {
    links.push({source: nodes[i-1], target: nodes[i]});
  }
  var linksl = [];
  for(var i=1;i<nodesl.length;++i) {
    linksl.push({source: nodesl[i-1], target: nodesl[i]});
  }
  console.log(links);
  vis.selectAll("circle.nodes")
    .data(nodes)
    .enter()
    .append("svg:circle")
    .attr("cx", function(d) { return xScale(d.x); })
    .attr("cy", function(d) { return yScale(d.y); })
    .attr("data-legend", function(d) { return "Standard"; })
    .attr("r", "6px")
    .attr("fill", "green");
  vis.selectAll("circle.nodes")
    .data(nodesl)
    .enter()
    .append("svg:circle")
    .attr("cx", function(d) { return xScale(d.x); })
    .attr("cy", function(d) { return yScale(d.y); })
    .attr("data-legend", function(d) { return "Legacy"; })
    .attr("r", "6px")
    .attr("fill", "blue");
  vis.selectAll(".line")
    .data(links)
    .enter()
    .append("line")
    .attr("x1", function(d) { return xScale(d.source.x); })
    .attr("y1", function(d) { return yScale(d.source.y); })
    .attr("x2", function(d) { return xScale(d.target.x); })
    .attr("y2", function(d) { return yScale(d.target.y); })
    .style("stroke", "rgb(0,255,0)");
  vis.selectAll(".line")
    .data(linksl)
    .enter()
    .append("line")
    .attr("x1", function(d) { return xScale(d.source.x); })
    .attr("y1", function(d) { return yScale(d.source.y); })
    .attr("x2", function(d) { return xScale(d.target.x); })
    .attr("y2", function(d) { return yScale(d.target.y); })
    .style("stroke", "rgb(0,0,255)");
   
  vis.append("svg:g")
    .attr("class", "axis")
    .attr("transform", "translate(0,591)")
    .call(xAxis);
  vis.append("svg:g")
    .attr("class", "axis")
    .attr("transform", "translate(50,0)")
    .call(yAxis);
  vis.append("svg:g")
    .attr("class", "legend")
    .attr("transform", "translate(70,50)")
    .call(d3.legend)
}