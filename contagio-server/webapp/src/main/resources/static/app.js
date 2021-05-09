$(function () {
    console.log('contagio server started...');

    $('.pass button.deletecommand').on('click', function (e) {
        $('#confirmdelete').modal('show');

        e.preventDefault();
    });

    $('#confirmdelete button.delete').on('click', () => {
        $('#confirmdelete').modal('hide');

        const form = $('#commandform')
        $('<input />')
            .attr('type', 'hidden')
            .attr('name', 'command')
            .attr('value', 'delete')
            .appendTo(form);

        form.submit();
    });
})
