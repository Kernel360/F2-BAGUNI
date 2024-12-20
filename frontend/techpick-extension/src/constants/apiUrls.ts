export const API_URLS = {
  getFoldersUrl: function () {
    return 'folders';
  },
  getBasicsFolderUrl: function () {
    return `${this.getFoldersUrl()}/basic`;
  },
  getPicksUrl: function () {
    return 'picks';
  },
  getPicksByLinkUrl: function (url: string) {
    return `${this.getPicksUrl()}/link-v2?link=${url}`;
  },
  getTagsUrl: function () {
    return 'tags';
  },
  getMoveTagsUrl: function () {
    return `${this.getTagsUrl}/location`;
  },
  getLinkUrl: function () {
    return 'links';
  },
  getLinkOGData: function (url: string) {
    return `${this.getLinkUrl()}?url=${url}`;
  },
};
