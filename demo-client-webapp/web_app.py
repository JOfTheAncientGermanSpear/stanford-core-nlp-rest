import hashlib
import json
import os
import re
import web

import sentence_form
import nlp_client
import tree_generator

render = web.template.render("templates")

urls = (
    '/', 'file_maker',
    '/parseresults/(.+)', 'parse_results'
)
app = web.application(urls, globals())


def parse_to_files(text):
        res_dir = 'static/{}'.format(hashlib.sha1(text).hexdigest())

        if os.path.exists(res_dir):
            return res_dir
        os.mkdir(res_dir)

        parse_res = nlp_client.parse(text)
        coref_parse_dep_trees = tree_generator.gen_from_json(parse_res)

        with open(res_dir + '/' + 'text.txt', 'w') as f:
            f.write(text)

        files = ['corefs.ps', 'phrase.ps', 'deps.ps']
        for (tree, f_name) in zip(coref_parse_dep_trees, files):
            f_path = os.path.join(res_dir, f_name)

            tree_generator.print_tree(tree, f_path)

        return res_dir


class file_maker:
    def GET(self):
        f = sentence_form.the_form()
        return render.home(f)

    def POST(self):
        f = sentence_form.the_form()
        f.validates()
        res_dir = parse_to_files(f.d.text)
        raise web.seeother('/parseresults/' + res_dir)


class parse_results:
    def GET(self, res_dir):
        return render.parse_results(res_dir)


def test():
    import doctest
    doctest.testmod()


if __name__ == "__main__":
    app.run()
