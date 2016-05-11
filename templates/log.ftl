<!DOCTYPE html>
<html lang="en">

<#include "metainfo.ftl">

  <body>

    <div class="container">

<#assign tab = "log">
<#include "navigation.ftl">

<h3>Log output</h3>





<div class="form-group">
  <label for="comment">Comment:</label>
  <textarea class="form-control" rows="5" id="comment"></textarea>
</div>


<div id="sse">
         <a href="javascript:WebSocketTest()">Run WebSocket</a>
      </div>


<#include "footer.ftl">

    </div> <!-- /container -->

</body>

<script type="text/javascript">
     function WebSocketTest()
     {
        if ("WebSocket" in window) {
           var ws = new WebSocket("ws://localhost:8080/events");
           var textArea = document.getElementById('comment');

           ws.onopen = function() {
              ws.send("HelloWorld");
              textArea.value += 'Connected..\n';
           };

           ws.onmessage = function(evt) { 
              textArea.value += evt.data;
           };

           ws.onclose = function() { 
              textArea.value += 'Closed\n'; 
           };
        }
        else {
           // The browser doesn't support WebSocket
           alert("WebSocket NOT supported by your Browser!");
        }
     }
</script>

</html>
