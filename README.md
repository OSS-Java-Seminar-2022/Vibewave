# VibeWave - Music Sharing App

## Table of Contents

* [General Info](#general-info)
* [Languages & Technologies](#languages--technologies)
* [Features](#features)
  * [Key Features](#key-features)
  * [Bonus Features](#bonus-features-if-time-allows)
* [Database Model](#database-model)
* [Authors](#authors)

## General Info

VibeWave is a platform for music distribution. You can share your own hard work or simply enjoy others, or even both - for free.

## Features

### Key Features
* User System (Login, signup with email confirmation, Logout, email password recover, changing password...)
  * Three roles: Basic User (limited features), Premium User, Admin
* Users can follow each other
* Users can upload their own albums (feature limited in some way for Basic users)
  * Publish date can be set up to 6 months in the future to notify followers and create hype
* All users, including guests, can listen to all published albums
  * Note: Number of plays is tracked, tho in a simple manner - each query is one play with no particular rules
* Users can create private or public playlists (which they can share with friends for example) and add tracks to it from various albums and artists
* Different listening sections
  * Fresh - sorted by date
  * Hot - sorted by number of total plays
  * Following - keeps users updated with their favorite artists
  * Staff selections - contains temporary playlists for promotion
* Verification system
  * In order for an artist to appear in public listening sections, they need to apply for a verification badge
    * Note: Exception to this rule is Staff Selections, which doesn't require verification
  * There are certain requirements to meet before a user can apply for verification
    * Note: Premium is not required for this

### Bonus Features (If Time Allows)
* Rating system
* Comments
* Real payment system to pay for Premium
* Users can pay to promote their music via Staff Selections

## Languages & Technologies
* Java
* Spring Boot
* HTML5
* Thymeleaf
* CSS3
* Tailwind
* UIkit
* JavaScript
* jQuery
* SQL
* PostgreSQL
* Docker

## Database Model

![VibeWave Database Model](https://i.imgur.com/paQRFoz.jpg)

## Authors

| Name | GitHub Link |
| --- | --- |
| Toni Kazinoti | [GitHub](https://github.com/tonikazinoti) |
| L. V. (withdrew) | [GitHub](https://github.com/banquetblintzs) |
