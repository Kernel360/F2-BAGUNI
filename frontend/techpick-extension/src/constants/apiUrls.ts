export const API_URLS = {
  getFoldersUrl: () => 'folders',
  getBasicsFolderUrl: function () {
    return `${this.getFoldersUrl()}/basic`;
  },
  getPicksUrl: () => 'picks',
  getPicksByLinkUrl: function (url: string) {
    return `${this.getPicksUrl()}/link?link=${url}`;
  },
  getTagsUrl: () => 'tags',
  getMoveTagsUrl: function () {
    return `${this.getTagsUrl()}/location`;
  },
  getLinkUrl: () => 'links',
  getExtensionUrl: () => 'extension',
  getVersion1Url: () => 'v1',
  getCreatePickUrl: function () {
    return `${this.getVersion1Url()}/${this.getExtensionUrl()}/${this.getPicksUrl()}`;
  },
};
