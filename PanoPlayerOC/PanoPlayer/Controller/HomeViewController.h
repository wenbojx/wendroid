//
//  HomeViewController.h
//  PanoPlayer
//
//  Created by 李文博 on 13-1-18.
//  Copyright (c) 2013年 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface HomeViewController : UIViewController{
    NSMutableArray *panoList;
    NSString *panoListUrl;
}

@property(retain, nonatomic) NSMutableArray *panoList;
@property(retain, nonatomic) NSString *panoListUrl;


- (void)addPano:(NSString *)panoId thumbImage:(NSString *)thumbImage panotitle:(NSString *)panoTitle photoTime:(NSString *)photoTime;

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath;
- (NSString *)getJsonFromUrl:(NSString *)url;




@end
