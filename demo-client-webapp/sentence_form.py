from web import form

the_form = form.Form(
    form.Textarea("text", description="Please Enter Some Text"),
    form.Button("submit", type="submit", description="Parse Text")
)
