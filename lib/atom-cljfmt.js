'use babel';

import AtomCljfmtView from './atom-cljfmt-view';
import { CompositeDisposable } from 'atom';

export default {

  atomCljfmtView: null,
  modalPanel: null,
  subscriptions: null,

  activate(state) {
    this.atomCljfmtView = new AtomCljfmtView(state.atomCljfmtViewState);
    this.modalPanel = atom.workspace.addModalPanel({
      item: this.atomCljfmtView.getElement(),
      visible: false
    });

    // Events subscribed to in atom's system can be easily cleaned up with a CompositeDisposable
    this.subscriptions = new CompositeDisposable();

    // Register command that toggles this view
    this.subscriptions.add(atom.commands.add('atom-workspace', {
      'atom-cljfmt:toggle': () => this.toggle()
    }));
  },

  deactivate() {
    this.modalPanel.destroy();
    this.subscriptions.dispose();
    this.atomCljfmtView.destroy();
  },

  serialize() {
    return {
      atomCljfmtViewState: this.atomCljfmtView.serialize()
    };
  },

  toggle() {
    console.log('AtomCljfmt was toggled!');
    return (
      this.modalPanel.isVisible() ?
      this.modalPanel.hide() :
      this.modalPanel.show()
    );
  }

};
