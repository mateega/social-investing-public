# Social Crypto Investing App In-Depth README


## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Despite a relatively massive shift towards collaborative and social trading, there is no easy way for retail investors to actually trade together while growing a community. Further, investing in cryptocurrencies is relatively difficult, with many investors wanting exposure to altcoins but not knowing enough about the crypto market to actually make trades themself.

This app aims to make cryptocurrency investing easier by allowing investors to trade together. Within the app, users can join groups and deposit paper money into the group’s pool of capital. Within each group, there are designated “investors” and “members.” Investors can trade the group’s pool of capital on behalf of the members. Each group is also equipt with a chat to encourage collaboration and communication before and after trades are executed. 

### App Evaluation
- **Category:** Finance, social.
- **Mobile:** Mobile-first Android app. 
- **Story:** Investing in crypto is hard. Invest smarter by investing with friends.
- **Market:** New crypto investors, college students, and young adults.

## Product Spec
### 1. User Stories 

**Required Must-have Stories**

* user can log in
* user can create a new account
* user can join multiple investing groups
* user can deposit paper money into a group
* user can make a trade on behalf of the group
* user can be either an investing or non-investing member (ability to make trades or not)
* user can chat others in the group

### 2. Screen Archetypes

* login page
    *  user can log in
*  registration page
    *   user can create a new account
*  groups page
    *  user can join multiple investing groups
    *  user can view multiple investing groups
*  group overview page
    *  overview of the group and its returns
*  deposit page
    *  user can deposit paper money into a group
*  trade page
    *  user can make a trade on behalf of the group
*  group setting page
    *  user can be either an investing or non-investing member (ability to make trades or not)
    *  ability to toggle members and investing or non-investing
*  chat page
    *  user can message others in the group

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* group overview page
* deposit page
* trade page
* group setting page
* chat page

**Flow Navigation** (Screen to Screen)

* login page
    * groups page
* registration page
    * groups page
* groups page
    * group overview page
    * deposit page
    * trade page
    * group setting page
    * chat page
* group overview, deposit, trade, group setting, chat
    * <>group overview, deposit, trade, group setting, chat<>

## Wireframes

https://github.com/mateega/metau-social-investing/blob/main/wireframe.pdf

## Schema 

### Models
User

| Property | Type | Description |
| -------- | -------- | -------- |
| objectId     | String     | email of user -- unique id for the user     |
| name     | String     | user's display name (first and/or last)     |
| password     | String     | password of users account     |
| email     | String     | email of user     |
| profilePicture     | String     | url to the profile picture of user     |
| assets     | Map<String, Number>     | amount of assets user has in total and different funds (eg: <total:10000, fund_1:2000>)


Group

| Property | Type | Description |
| -------- | -------- | -------- |
| objectId     | String     | unqiue id for the group, name of group in lowercase with spaces as underscores     |
| name     | String     | name of group/fund     |
| assets     | Number     | Total assets in fund pooled from users    |
| members     | ArrayList<String>     | list of non-investing members in group     |
| investors     | ArrayList<String?     | list of investing members in group     |
| trades     | ArrayList<Map<String, Object>>     | map of data for each trade -- direction (String, "buy" or "sell"), lot (Number), price (Number), ticker (String), time (Timestamp), trader (String)   |


Chat

| Property | Type | Description |
| -------- | -------- | -------- |
| objectId     | String     | unique id for a group's chat, same value as Group objectId     |
| messages     | ArrayList<Message>     | list of the group's messages     |


Message

| Property | Type | Description |
| -------- | -------- | -------- |
| objectId     | String     | unique id for the message, simply incremented from the previous message (eg: "17")     |
| user     | String     | id of user who sent message     |
| body     | String     | body text of the message     |
| time | TimeStamp | time that the message was sent |


### Networking
For my project I will be using Firebase. Within Firebase, I chose to go with Cloud Firestore over Realtime Database since data is stored as collections of documents. This leads to complex, hierarchical data being easier to organize at scale compared to Realtime Databases which used one large JSON tree to store data. 


* login page
    *  (Read/GET) check for user account
*  registration page
    *   (Create/POST) add user account
*  groups page
    *  (Read/GET) query all groups to display group name and asset amount
*  group overview page
    *  (Read/GET) query specific group to get member count, assets count and most recent trade
    *  (Read/GET) query user to get personal asset count
*  deposit page
    *  (Read/GET) query user to get asset count
    *  (Update/PUT) update user's asset count
*  chat page
    *  (Read/GET) query specific chat to get messages
    *  (Create/POST) create a new message
*  trade: search page
    *  no network request needed, only dealing with external (non-database) API(s)
*  trade: info page
    * no network request needed, only dealing with external (non-database) API(s)
*  trade: payment page
    *  (Create/POST) create a new trade for a group
*  group setting page
    *  (Read/GET) query group to get members and investors
    *  (Delete/DELETE) remove user from group if they would like to leave
