from __future__ import division


import json
from nltk import Tree


def _gen_dependencies_tree(deps, gov_map=None, root_ix=None):

    def mk_label(x):
        return 'word: {}, index: {}'.format(x['value'], x['index'])

    if gov_map is None:
        gov_map = {d['governor']['index']: Tree(mk_label(d['governor']), [])
                   for d in deps}

    if root_ix is None:
        child_ixs = {d['dependent']['index'] for d in deps}
        governor_ixs = set(gov_map.keys())
        root_ix = governor_ixs.difference(child_ixs)

    for d in deps:
        dep = d['dependent']
        dep_ix = dep['index']

        is_leaf = dep_ix not in gov_map

        dep_label = mk_label(dep)

        dep = [dep_label] if is_leaf else gov_map[dep_ix]

        gov_ix = d['governor']['index']

        gov = gov_map[gov_ix]
        rel = d['relation']
        gov.append(Tree(rel['value'], [dep]))

    return [gov_map[r] for r in root_ix]


def _gen_phrase_structure_tree(phrase):
    label = phrase['category']

    is_leaf = 'constituents' not in phrase
    if is_leaf:
        return label

    sorted_children = sorted(phrase['constituents'], key=lambda e: e['index'])
    sorted_children = map(_gen_phrase_structure_tree, sorted_children)
    return Tree(label, sorted_children)


def _gen_coref_tree(coref):
    def gen_mentions_tree(m):
        word_field = 'start: ' + str(m['start'])
        sent_field = 'sent: ' + str(m['sentence'])
        word_tree = Tree(word_field, [m['text']])
        return Tree(sent_field, [word_tree])

    mentions = coref['mentions']

    return [gen_mentions_tree(m) for m in mentions] if len(mentions) > 1 else []


def gen_from_json(content):

    fn_map = {
        'coreferences': {'fn': _gen_coref_tree,
                         'is_array': 1,
                         'id': 'id',
                         'child_filter_fn': lambda c: len(c['mentions']) > 1
                         },
        'sentences':
            {'phrase_structure': {'fn': _gen_phrase_structure_tree},
             'dependencies': {'fn': _gen_dependencies_tree},
             'is_array': 1,
             'id': 'index'}
    }

    def safe_load(field_path, fn_map=fn_map, content=content):
        field = field_path[0]
        field_path = field_path[1:]
        fn_map = fn_map[field]
        is_array = fn_map['is_array'] if 'is_array' in fn_map else 0
        if is_array:
            id_fn = lambda e: e[fn_map['id']]

        if field not in content:
            return None

        content = content[field]

        def gen_children(fn):
            sorted_children = sorted(content, key=id_fn)

            tree_with_id = lambda c: Tree('id: {}'.format(id_fn(c)), fn(c))

            child_filter_fn = fn_map.get('child_filter_fn')
            return [tree_with_id(c) for c in sorted_children if
                    (not child_filter_fn or child_filter_fn(c))]

        if not field_path:
            fn = fn_map['fn']
            res = lambda: Tree(field, gen_children(fn)) if is_array else fn(content)
        else:
            if is_array:
                load_child = lambda c: safe_load(field_path, fn_map, c)
                res = lambda: Tree(field, gen_children(load_child))
            else:
                res = lambda: safe_load(field_path, fn_map, content)

        return res()

    corefs_tree = safe_load(['coreferences'])
    phrase_structures_tree = safe_load(['sentences', 'phrase_structure'])
    dependencies_tree = safe_load(['sentences', 'dependencies'])
    return corefs_tree, phrase_structures_tree, dependencies_tree


#http://stackoverflow.com/questions/23429117/saving-nltk-drawn-parse-tree-to-image-file
def print_tree(t, file_name):
    from nltk.draw.tree import TreeView
    tv = TreeView(t)
    tv._cframe.print_to_file(file_name)
