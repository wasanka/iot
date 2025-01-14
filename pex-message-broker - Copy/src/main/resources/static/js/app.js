document.addEventListener("DOMContentLoaded", function() {

    function createTD(tr, id){
        td = document.createElement('td');
        td.id = id;
        tr.appendChild(td);

        return td;
    }

    // Function to fetch JSON data from a relative URL
    function fetchJsonAndModifyElement() {
        fetch('/status') // Adjust the relative URL as needed
            .then(response => response.json())
            .then(data => {
                // Assuming the JSON has a key 'message'
                const queueTableHeader = document.getElementById('queueTableHeader');
                const queueTableBody = document.getElementById('queueTableBody');
                //contentElement.textContent = data.body['QUEUE_STATUS'][0].id;

                Object.values(data.body['MEMBERS']).forEach(m => {
                    th = document.createElement('th');
                    th.textContent = m;
                    queueTableHeader.appendChild(th);
                });
                th = document.createElement('th');
                th.textContent = 'Total';
                queueTableHeader.appendChild(th);

                const queueMap = {};

                data.body['QUEUE_STATUS'].forEach(qs => {

                    if (!queueMap[qs.queue]) {
                        queueMap[qs.queue] = { depth: [], pending: [], completed: [] };
                    }

                    queueMap[qs.queue].depth.push(qs.depth);
                    queueMap[qs.queue].pending.push(qs.pending);
                    queueMap[qs.queue].completed.push(qs.completed);

                    var depth_tr = queueTableBody.querySelector('#row_depth_' + qs.queue);
                    if(depth_tr === null){

                        depth_tr = document.createElement('tr');
                        depth_tr.id = 'row_depth_' + qs.queue;
                        queueTableBody.appendChild(depth_tr);

                        var td = createTD(depth_tr, 'td_depth_' + qs.queue);
                        td.rowSpan='3';
                        td.textContent= qs.queue;
                        td = createTD(depth_tr, 'td_depth_' + qs.queue);
                        td.textContent='Depth';

                        Object.keys(data.body['MEMBERS']).forEach(m => {
                            td = document.createElement('td');
                            td.id = 'td_depth_' + m + '_' + qs.queue;
                            td.textContent = '0';
                            depth_tr.appendChild(td);
                        });

                        td = document.createElement('td');
                        td.id = 'td_depth_total_' + qs.queue;
                        td.textContent = '0';
                        depth_tr.appendChild(td);
                    }

                    var pending_tr = queueTableBody.querySelector('#row_pending_' + qs.queue);
                    if(pending_tr === null){

                        pending_tr = document.createElement('tr');
                        pending_tr.id = 'row_pending_' + qs.queue;
                        queueTableBody.appendChild(pending_tr);

                        var td = createTD(pending_tr, 'td_pending_' + qs.queue);
                        td.textContent='Pending';

                        Object.keys(data.body['MEMBERS']).forEach(m => {
                            td = document.createElement('td');
                            td.id = 'td_pending_' + m + '_' + qs.queue;
                            td.textContent = '0';
                            pending_tr.appendChild(td);
                        });

                        td = document.createElement('td');
                        td.id = 'td_pending_total_' + qs.queue;
                        td.textContent = '0';
                        pending_tr.appendChild(td);
                    }

                    var completed_tr = queueTableBody.querySelector('#row_completed_' + qs.queue);
                    if(completed_tr === null){

                        completed_tr = document.createElement('tr');
                        completed_tr.id = 'row_completed_' + qs.queue;
                        queueTableBody.appendChild(completed_tr);

                        var td = createTD(completed_tr, 'td_completed_' + qs.queue);
                        td.textContent='Completed';

                        Object.keys(data.body['MEMBERS']).forEach(m => {
                            td = document.createElement('td');
                            td.id = 'td_completed_' + m + '_' + qs.queue;
                            td.textContent = '0';
                            completed_tr.appendChild(td);
                        });

                        td = document.createElement('td');
                        td.id = 'td_completed_total_' + qs.queue;
                        td.textContent = '0';
                        completed_tr.appendChild(td);
                    }
                });

                data.body['QUEUE_STATUS'].forEach(qs => {
                    var depth_td = queueTableBody.querySelector('#td_depth_' + qs.memberId + '_' + qs.queue);
                    depth_td.textContent = qs.depth;
                    var pending_td = queueTableBody.querySelector('#td_pending_' + qs.memberId + '_' + qs.queue);
                    pending_td.textContent = qs.pending;
                    var completed_td = queueTableBody.querySelector('#td_completed_' + qs.memberId + '_' + qs.queue);
                    completed_td.textContent = qs.completed;

                    var depth_total_td = queueTableBody.querySelector('#td_depth_total_' + qs.queue);
                    depth_total_td.textContent = queueMap[qs.queue].depth.reduce((a, b) => a + b, 0);

                    var pending_total_td = queueTableBody.querySelector('#td_pending_total_' + qs.queue);
                    pending_total_td.textContent = queueMap[qs.queue].pending.reduce((a, b) => a + b, 0);

                    var completed_total_td = queueTableBody.querySelector('#td_completed_total_' + qs.queue);
                    completed_total_td.textContent = queueMap[qs.queue].completed.reduce((a, b) => a + b, 0);
                });

                var memberTableBody = document.querySelector('#memberTableBody');

                data.body['NETWORK'].forEach(ms => {

                    tr = document.createElement('tr');
                    td = document.createElement('td');

                    divForA = document.createElement('button');
//                    href = document.createElement('a');
                    divForA.textContent=ms.id;
//                    href.setAttribute('href', '/status');
//                    href.setAttribute('hx-trigger', 'load');
//                    divForA.setAttribute('hx-boost', 'true');
                    divForA.setAttribute('hx-get', '/index?page=0&pageSize=10&type=MEMBER_DASHBOARD&memberId='+ms.id);
                    divForA.setAttribute('hx-target', '#memberDetails');
                    divForA.setAttribute('hx-swap', 'innerHTML');
                    td.appendChild(divForA);
//                    divForA.appendChild(href);

                    td.setAttribute('class', 'my-new-div');

                    tr.appendChild(td);

                    td = document.createElement('td');
                    td.textContent=data.body['MEMBERS'][ms.id];
                    tr.appendChild(td);

                    td = document.createElement('td');
                    td.textContent=ms.consumers;
                    tr.appendChild(td);

                    td = document.createElement('td');
                    td.textContent=ms.publishers;
                    tr.appendChild(td);

                    td = document.createElement('td');
                    td.textContent=ms.usedMemoryMb;
                    tr.appendChild(td);

                    td = document.createElement('td');
                    td.textContent=ms.dbFileSize;
                    tr.appendChild(td);

                    memberTableBody.appendChild(tr);
                });

                htmx.process(document.body);
            })
            .catch(error => {
                console.error('Error fetching JSON:', error);
            });
    }

    // Call the function to fetch JSON and modify the HTML element
    fetchJsonAndModifyElement();
});