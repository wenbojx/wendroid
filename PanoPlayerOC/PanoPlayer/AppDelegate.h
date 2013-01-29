//
//  AppDelegate.h
//  PanoPlayer
//
//  Created by 李文博 on 13-1-18.
//  Copyright (c) 2013年 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate>{
    UINavigationController *navHomeController;
    UINavigationController *navInfoController;
    UINavigationController *navMapController;
    UITabBarController *tabBarController;
}

@property (strong, nonatomic) UIWindow *window;

@end
