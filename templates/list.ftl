<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>
  </title>
  <style type="text/css">
  td.icon {
   width:16px;
   height:16px;
   vertical-align:center;
  }
  td.dir {
  	background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABq0lEQVQ4y8WTu4oUQRSGv+rtGVuxhwVFdFEEE2c3d0HYTEMTn8DEVxADQTDUF9DMwMxQMBMx8AEWzRQ3cBHd9TI91+2urjq/QbczY2IygSep4nD+79yqnCRWsYQVbWVACvDh5ZXdrLe15dwyT1TjT/sxFFeB6i+VA2B6+cb7kAI4Jf0LO087zjlQI8Y5Qvnj0sHug321XoC1bk+K9eHk6+s7wPMUgKAS88eqb4+Jfg2SHs7lZBvX2Nh+2EUCDGSAcMnJsx9f7NxfAGqXyDzRd5EJO/pMPT1gcviGTnYOVIN5pAAE8v7dLrKL8xnglFk4ws9Afko9HpH3b5Gd2mwb/lOBmgrSdYhJugDUCenxM6xv3p4HCsP8F0LxCsUhCkMURihOyM7fg0osASTFEpu9a4LjGIUCqwcoDiEUrX+E4hRUQb20RiokC1j9vckUhygU7X3QZh7NAVKYL7YBeMkRUfjVCotF2XGIwnghtrJpMywB5G0QZj9P1JNujuWJ1AHLQadRrACPkuZ0SSSWpeStWgDK6tHek5vbiOs48n++XQHurcf0rFng//6NvwG+iB9/4duaTgAAAABJRU5ErkJgggo=) no-repeat;
}
  td.file {
  background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAA6ElEQVQoFQXBMW5TQRgGwNnHnoE0QbiCjoIooUmTU3AuS1BwIoTSUdJBigg3GCWOg9/++zHTop078wIAsPMrE4SL5/1aIyMjIyMjz/m0tbFECFdrPeaQQw75mz/5nZH7fN7aWILmauSYfznmmIfss8vIUx7zZWsTTXM5vpWvTk5Wq9VHQP/gtgOLa0Qpw940vAQdaG6thpOhlOkG0AEuAVGmEkAH+G4YSikxXQM6wDsAMRFAB/ihDNNUmN4DOsAbBAEAdICfpmmaAt4COoj2GgCASbIkZh1NAACznhQt2itnFgAAlF3u/gMDtJXPzQxoswAAAABJRU5ErkJgggo=) no-repeat;
}
  </style>
  <link rel="shortcut icon" href="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAA6ElEQVQoFQXBMW5TQRgGwNnHnoE0QbiCjoIooUmTU3AuS1BwIoTSUdJBigg3GCWOg9/++zHTop078wIAsPMrE4SL5/1aIyMjIyMjz/m0tbFECFdrPeaQQw75mz/5nZH7fN7aWILmauSYfznmmIfss8vIUx7zZWsTTXM5vpWvTk5Wq9VHQP/gtgOLa0Qpw940vAQdaG6thpOhlOkG0AEuAVGmEkAH+G4YSikxXQM6wDsAMRFAB/ihDNNUmN4DOsAbBAEAdICfpmmaAt4COoj2GgCASbIkZh1NAACznhQt2itnFgAAlF3u/gMDtJXPzQxoswAAAABJRU5ErkJgggo=" />
</head>
<body>
<div style="width:900px; margin:auto">
  <table class="file browser">
    <thead>
    <tr class="header">
      <th colspan="4">${currentPath}</th>
    </tr>
    <tr class="header">
      <th></th>
      <th>name</th>
      <th>size</th>
      <th>age</th>
    </tr>
    </thead>
    <tbody>
    <#list files as file>
        <#if !file.resource>
	      <tr>
	        <#if file.type == "dir">
	        	<td class="icon dir"></td>
	        </#if>
	        <#if file.type == "file">
		        <td class="icon file"></td>
	        </#if>
	        <td class="content"> <a href="/${file.url}">${file.name}</a></td>
	        <td class="size">
	        ${file.size}
	        <#if file.type == "dir">items</#if>
	        <#if file.type == "file">bytes</#if>
	        </td>
	        <td class="age">${file.lastModifiedDate}</td>
	      </tr>
        </#if>
      </#list>
    </tbody>
  </table>
  </div>
</body>
</html>